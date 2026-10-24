package br.com.fiap.ReceiptApi.infrastructure.adapter.out;

import br.com.fiap.ReceiptApi.application.port.out.ProcessedEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** SQL PostgreSQL mantém a disputa concorrente pela identidade do evento no banco. */
@Repository
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProcessedEventRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean registerIfAbsent(UUID eventId, UUID paymentId, long aggregateVersion) {
        int inserted = jdbcTemplate.update("""
                INSERT INTO PROCESSED_EVENTS (EVENT_ID, PAYMENT_ID, AGGREGATE_VERSION)
                VALUES (?, ?, ?)
                ON CONFLICT (EVENT_ID) DO NOTHING
                """, eventId, paymentId, aggregateVersion);
        return inserted == 1;
    }
}
