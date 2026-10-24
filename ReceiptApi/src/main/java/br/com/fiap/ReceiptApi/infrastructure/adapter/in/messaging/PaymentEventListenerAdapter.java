package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ProcessPaymentEventUseCase;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.PaymentProcessedEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class PaymentEventListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListenerAdapter.class);

    private final ProcessPaymentEventUseCase processPaymentEvent;

    public PaymentEventListenerAdapter(ProcessPaymentEventUseCase processPaymentEvent) {
        this.processPaymentEvent = processPaymentEvent;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.receipt}")
    public void onPaymentProcessed(PaymentProcessedEventDTO event){
        logger.info("Evento de pagamento recebido: {} com status {}", event.getPaymentId(), event.getStatus());

        UUID eventId = event.getEventId() == null ? legacyEventId(event) : event.getEventId();
        long aggregateVersion = event.getAggregateVersion() == null
                ? ("REFUNDED".equals(event.getStatus()) ? 2L : 1L)
                : event.getAggregateVersion();
        PaymentVO paymentVO = new PaymentVO(
                event.getPaymentId(),
                event.getPaymentMethod(),
                event.getAmount(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getProcessedAt());

        // O caso de uso só retorna após commit; o container confirma a entrega após o retorno.
        processPaymentEvent.process(eventId, aggregateVersion, paymentVO);
    }

    private UUID legacyEventId(PaymentProcessedEventDTO event) {
        String identity = event.getPaymentId() + ":" + event.getStatus() + ":" + event.getProcessedAt();
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }
}
