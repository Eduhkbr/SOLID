package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.PaymentProcessedEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListenerAdapter {

    private final ReceiptService receiptService;

    public PaymentEventListenerAdapter(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.receipt}")
    public void onPaymentProcessed(PaymentProcessedEventDTO event){
        System.out.println("LOG: Evento recebido do RabbitMQ: " + event.getPaymentId());

        PaymentVO paymentVO = new PaymentVO(
                event.getPaymentId(),
                event.getPaymentMethod(),
                event.getAmount(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getProcessedAt());

        receiptService.createReceiptFromPaymentInfo(paymentVO);
    }
}
