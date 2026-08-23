package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentEventPublisherPort;
import br.com.fiap.solid.contracts.event.v1.PaymentProcessedEvent;
import br.com.fiap.solid.contracts.event.v1.PaymentRefundedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPaymentPublisherAdapter implements PaymentEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final String paymentExchange;
    private final String receiptProcessedRoutingKey;
    private final String receiptRefundedRoutingKey;

    public RabbitMQPaymentPublisherAdapter(RabbitTemplate rabbitTemplate,
                                           @Value("${app.rabbitmq.exchange.payment}") String paymentExchange,
                                           @Value("${app.rabbitmq.routingkey.receipt}") String receiptProcessedRoutingKey,
                                           @Value("${app.rabbitmq.routingkey.receipt.refunded}") String receiptRefundedRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.paymentExchange = paymentExchange;
        this.receiptProcessedRoutingKey = receiptProcessedRoutingKey;
        this.receiptRefundedRoutingKey = receiptRefundedRoutingKey;
    }

    @Override
    public void publishPaymentProcessedEvent(Payment payment) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                payment.getId(),
                payment.getPaymentMethod().name(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
        rabbitTemplate.convertAndSend(paymentExchange, receiptProcessedRoutingKey, event);
    }

    @Override
    public void publishPaymentRefundedEvent(Payment payment) {
        PaymentRefundedEvent event = new PaymentRefundedEvent(
                payment.getId(),
                payment.getPaymentMethod().name(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
        rabbitTemplate.convertAndSend(paymentExchange, receiptRefundedRoutingKey, event);
    }
}
