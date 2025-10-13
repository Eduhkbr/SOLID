package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging.dto;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentProcessedEventDTOTest {
    @Test
    void testAllArgsConstructorAndGettersSetters() {
        UUID id = UUID.randomUUID();
        String method = "PIX";
        BigDecimal amount = new BigDecimal("100.00");
        String status = "PENDING";
        LocalDateTime processedAt = LocalDateTime.now();
        PaymentProcessedEventDTO dto = new PaymentProcessedEventDTO(id, method, amount, status, processedAt);
        assertThat(dto.getPaymentId()).isEqualTo(id);
        assertThat(dto.getPaymentMethod()).isEqualTo(method);
        assertThat(dto.getAmount()).isEqualByComparingTo(amount);
        assertThat(dto.getStatus()).isEqualTo(status);
        assertThat(dto.getProcessedAt()).isEqualTo(processedAt);
        // setters
        UUID newId = UUID.randomUUID();
        dto.setPaymentId(newId);
        assertThat(dto.getPaymentId()).isEqualTo(newId);
    }

    @Test
    void testNoArgsConstructor() {
        PaymentProcessedEventDTO dto = new PaymentProcessedEventDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void testMapperConstructorFromPayment() {
        UUID id = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        LocalDateTime updatedAt = LocalDateTime.now();
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(id);
        when(payment.getPaymentMethod()).thenReturn(Payment.PaymentMethod.PIX);
        when(payment.getAmount()).thenReturn(amount);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(payment.getUpdatedAt()).thenReturn(updatedAt);
        PaymentProcessedEventDTO dto = new PaymentProcessedEventDTO(payment);
        assertThat(dto.getPaymentId()).isEqualTo(id);
        assertThat(dto.getPaymentMethod()).isEqualTo("PIX");
        assertThat(dto.getAmount()).isEqualByComparingTo(amount);
        assertThat(dto.getStatus()).isEqualTo("PENDING");
        assertThat(dto.getProcessedAt()).isEqualTo(updatedAt);
    }
}

