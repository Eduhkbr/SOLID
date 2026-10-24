package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentEventPublisherPort;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging.dto.PaymentProcessedEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Persiste a intenção de publicar na mesma transação local que altera o pagamento. */
@Component
public class PaymentEventOutboxAdapter implements PaymentEventPublisherPort {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventOutboxAdapter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishPaymentStateChangedEvent(Payment payment) {
        UUID eventId = UUID.randomUUID();
        long aggregateVersion = payment.getStatus() == PaymentStatus.REFUNDED ? 2L : 1L;
        PaymentProcessedEventDTO event = new PaymentProcessedEventDTO(payment, eventId, aggregateVersion);
        try {
            String payload = objectMapper.writeValueAsString(event);
            jdbcTemplate.update("""
                    INSERT INTO PAYMENT_OUTBOX
                        (EVENT_ID, AGGREGATE_ID, AGGREGATE_VERSION, EVENT_TYPE, PAYLOAD)
                    VALUES (?, ?, ?, ?, CAST(? AS JSONB))
                    """, eventId, payment.getId(), aggregateVersion,
                    "PaymentStateChanged", payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o evento de pagamento", exception);
        }
    }
}
