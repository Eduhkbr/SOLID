package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.solid.contracts.event.v1.PaymentProcessedEvent;
import br.com.fiap.solid.contracts.event.v1.PaymentRefundedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${app.rabbitmq.queue.receipt}")
public class PaymentEventListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListenerAdapter.class);

    private final ReceiptService receiptService;

    public PaymentEventListenerAdapter(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @RabbitHandler
    public void onPaymentProcessed(PaymentProcessedEvent event){
        logger.info("Evento de pagamento processado recebido: {}", event.paymentId());
        PaymentVO paymentVO = new PaymentVO(
                event.paymentId(),
                event.paymentMethod(),
                event.amount(),
                event.status(),
                event.createdAt(),
                event.processedAt());
        receiptService.createReceiptFromPaymentInfo(paymentVO);
    }

    @RabbitHandler
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        logger.info("Evento de pagamento estornado recebido: {}", event.paymentId());
        PaymentVO paymentVO = new PaymentVO(
                event.paymentId(),
                event.paymentMethod(),
                event.amount(),
                event.status(),
                event.createdAt(),
                event.refundedAt());
        receiptService.updateForRefund(paymentVO);
    }
}
