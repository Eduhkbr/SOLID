package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Repository
public class PaymentOutboxRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentOutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public List<OutboxMessage> claimBatch(int batchSize) {
        return jdbcTemplate.query("""
                WITH candidates AS (
                    SELECT EVENT_ID
                    FROM PAYMENT_OUTBOX
                    WHERE PUBLISHED_AT IS NULL
                      AND NEXT_ATTEMPT_AT <= CURRENT_TIMESTAMP
                      AND (LOCKED_UNTIL IS NULL OR LOCKED_UNTIL < CURRENT_TIMESTAMP)
                    ORDER BY CREATED_AT
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?
                )
                UPDATE PAYMENT_OUTBOX AS event
                SET LOCKED_UNTIL = CURRENT_TIMESTAMP + INTERVAL '30 seconds',
                    ATTEMPT_COUNT = ATTEMPT_COUNT + 1
                FROM candidates
                WHERE event.EVENT_ID = candidates.EVENT_ID
                RETURNING event.EVENT_ID, event.PAYLOAD::text,
                          event.TRACE_CONTEXT::text AS trace_context, event.ATTEMPT_COUNT
                """, (result, row) -> new OutboxMessage(
                result.getObject("EVENT_ID", UUID.class),
                result.getString("PAYLOAD"),
                result.getString("trace_context"),
                result.getInt("ATTEMPT_COUNT")), batchSize);
    }

    @Transactional
    public void markPublished(UUID eventId) {
        jdbcTemplate.update("""
                UPDATE PAYMENT_OUTBOX
                SET PUBLISHED_AT = CURRENT_TIMESTAMP, LOCKED_UNTIL = NULL, LAST_ERROR = NULL
                WHERE EVENT_ID = ? AND PUBLISHED_AT IS NULL
                """, eventId);
    }

    @Transactional
    public void scheduleRetry(UUID eventId, Duration delay, String reason) {
        String safeReason = reason == null ? "Falha de publicação sem detalhe" : reason;
        if (safeReason.length() > 1000) safeReason = safeReason.substring(0, 1000);
        jdbcTemplate.update("""
                UPDATE PAYMENT_OUTBOX
                SET LOCKED_UNTIL = NULL,
                    NEXT_ATTEMPT_AT = CURRENT_TIMESTAMP + (? * INTERVAL '1 millisecond'),
                    LAST_ERROR = ?
                WHERE EVENT_ID = ? AND PUBLISHED_AT IS NULL
                """, delay.toMillis(), safeReason, eventId);
    }

    public long pendingCount() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PAYMENT_OUTBOX WHERE PUBLISHED_AT IS NULL", Long.class);
        return count == null ? 0L : count;
    }

    public long oldestPendingAgeSeconds() {
        Long age = jdbcTemplate.queryForObject("""
                SELECT COALESCE(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - MIN(CREATED_AT))), 0)::bigint
                FROM PAYMENT_OUTBOX
                WHERE PUBLISHED_AT IS NULL
                """, Long.class);
        return age == null ? 0L : age;
    }
}
