package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging.dto.PaymentProcessedEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RabbitMQPaymentPublisherAdapterTest {
    private RabbitTemplate rabbitTemplate;
    private RabbitMQPaymentPublisherAdapter adapter;

    @BeforeEach
    void setup() {
        rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        adapter = new RabbitMQPaymentPublisherAdapter(rabbitTemplate, "exchange", "routingKey");
    }

    @Test
    void publishPaymentProcessedEvent_sendsMessage() {
        Payment payment = Mockito.mock(Payment.class);
        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(new BigDecimal("10.00"));
        when(payment.getPaymentMethod()).thenReturn(Payment.PaymentMethod.PIX);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        doNothing().when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(PaymentProcessedEventDTO.class));
        adapter.publishPaymentProcessedEvent(payment);
        verify(rabbitTemplate, times(1)).convertAndSend(any(String.class), any(String.class), any(PaymentProcessedEventDTO.class));
    }

    @Test
    void publishPaymentProcessedEvent_handlesException() {
        Payment payment = Mockito.mock(Payment.class);
        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getAmount()).thenReturn(new BigDecimal("10.00"));
        when(payment.getPaymentMethod()).thenReturn(Payment.PaymentMethod.PIX);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        doThrow(new RuntimeException("erro")).when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(PaymentProcessedEventDTO.class));
        assertThatThrownBy(() -> adapter.publishPaymentProcessedEvent(payment))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("erro");
        verify(rabbitTemplate, times(1)).convertAndSend(any(String.class), any(String.class), any(PaymentProcessedEventDTO.class));
    }
}
