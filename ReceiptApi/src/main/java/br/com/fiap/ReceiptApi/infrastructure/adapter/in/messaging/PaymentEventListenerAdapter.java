package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.PaymentProcessedEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class PaymentEventListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListenerAdapter.class);

    private final ReceiptService receiptService;
    private final JdbcTemplate jdbcTemplate;

    public PaymentEventListenerAdapter(ReceiptService receiptService, JdbcTemplate jdbcTemplate) {
        this.receiptService = receiptService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.receipt}")
    @Transactional
    public void onPaymentProcessed(PaymentProcessedEventDTO event){
        logger.info("Evento de pagamento recebido: {} com status {}", event.getPaymentId(), event.getStatus());

        UUID eventId = event.getEventId() == null ? legacyEventId(event) : event.getEventId();
        long aggregateVersion = event.getAggregateVersion() == null
                ? ("REFUNDED".equals(event.getStatus()) ? 2L : 1L)
                : event.getAggregateVersion();
        int inserted = jdbcTemplate.update("""
                INSERT INTO PROCESSED_EVENTS (EVENT_ID, PAYMENT_ID, AGGREGATE_VERSION)
                VALUES (?, ?, ?)
                ON CONFLICT (EVENT_ID) DO NOTHING
                """, eventId, event.getPaymentId(), aggregateVersion);
        if (inserted == 0) {
            logger.info("Evento {} já foi aplicado; reentrega ignorada.", eventId);
            return;
        }

        PaymentVO paymentVO = new PaymentVO(
                event.getPaymentId(),
                event.getPaymentMethod(),
                event.getAmount(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getProcessedAt());

        if ("REFUNDED".equals(event.getStatus())) {
            receiptService.updateForRefund(paymentVO);
        } else {
            receiptService.createReceiptFromPaymentInfo(paymentVO);
        }
    }

    private UUID legacyEventId(PaymentProcessedEventDTO event) {
        String identity = event.getPaymentId() + ":" + event.getStatus() + ":" + event.getProcessedAt();
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }
}
