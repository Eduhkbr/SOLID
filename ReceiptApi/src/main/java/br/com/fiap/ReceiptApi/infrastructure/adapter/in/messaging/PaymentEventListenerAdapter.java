package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.PaymentProcessedEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListenerAdapter.class);

    private final ReceiptService receiptService;

    public PaymentEventListenerAdapter(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.receipt}")
    public void onPaymentProcessed(PaymentProcessedEventDTO event){
        logger.info("Evento de pagamento recebido: {} com status {}", event.getPaymentId(), event.getStatus());

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
}
