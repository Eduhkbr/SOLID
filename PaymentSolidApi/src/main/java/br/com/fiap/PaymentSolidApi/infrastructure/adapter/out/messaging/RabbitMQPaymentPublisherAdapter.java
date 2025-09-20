package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentEventPublisherPort;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging.dto.PaymentProcessedEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPaymentPublisherAdapter implements PaymentEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final String paymentExchange;
    private final String receiptRoutingKey;

    public RabbitMQPaymentPublisherAdapter(RabbitTemplate rabbitTemplate,
                                           @Value("${app.rabbitmq.exchange.payment}") String paymentExchange,
                                           @Value("${app.rabbitmq.routingkey.receipt}") String receiptRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.paymentExchange = paymentExchange;
        this.receiptRoutingKey = receiptRoutingKey;
    }

    @Override
    public void publishPaymentProcessedEvent(Payment payment) {
        // 1. Converte o objeto de domínio para o DTO do evento.
        PaymentProcessedEventDTO eventDTO = new PaymentProcessedEventDTO(payment);

        // 2. Envia a mensagem para a exchange com a routing key específica.
        rabbitTemplate.convertAndSend(paymentExchange, receiptRoutingKey, eventDTO);
    }
}
