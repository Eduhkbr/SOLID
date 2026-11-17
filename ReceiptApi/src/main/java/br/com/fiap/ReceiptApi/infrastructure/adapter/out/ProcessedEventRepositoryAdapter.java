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
    public void lockPayment(UUID paymentId) {
        // Lock transacional no PostgreSQL também coordena consumidores em outras JVMs.
        // Colisões do hash só serializam pagamentos adicionais; não violam consistência.
        jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
            try (var statement = connection.prepareStatement("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))")) {
                statement.setString(1, paymentId.toString());
                statement.execute();
            }
            return null;
        });
    }

    @Override
    public long latestVersion(UUID paymentId) {
        Long value = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(AGGREGATE_VERSION), 0) FROM PROCESSED_EVENTS WHERE PAYMENT_ID = ?", Long.class, paymentId);
        return value == null ? 0 : value;
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
