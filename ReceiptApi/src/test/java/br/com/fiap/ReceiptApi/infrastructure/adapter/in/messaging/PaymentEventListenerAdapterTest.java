package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ProcessPaymentEventUseCase;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.PaymentProcessedEventDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class PaymentEventListenerAdapterTest {

    @Test
    void mapsPaymentEventAndDelegatesProcessing() {
        ProcessPaymentEventUseCase useCase = mock(ProcessPaymentEventUseCase.class);
        PaymentEventListenerAdapter adapter = new PaymentEventListenerAdapter(useCase);
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2025, 2, 3, 10, 15);
        LocalDateTime processedAt = createdAt.plusDays(1);
        PaymentProcessedEventDTO event = new PaymentProcessedEventDTO(
                paymentId, "CREDIT_CARD", new BigDecimal("42.50"), "REFUNDED", createdAt, processedAt,
                eventId, 2L);

        adapter.onPaymentProcessed(event);

        verify(useCase).process(eventId, 2L, new PaymentVO(
                paymentId, "CREDIT_CARD", new BigDecimal("42.50"), "REFUNDED", createdAt, processedAt));
    }

    @Test
    void derivesStableLegacyIdentityWhenEventHasNoIdOrVersion() {
        ProcessPaymentEventUseCase useCase = mock(ProcessPaymentEventUseCase.class);
        PaymentEventListenerAdapter adapter = new PaymentEventListenerAdapter(useCase);
        UUID paymentId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.of(2025, 2, 3, 10, 15);
        PaymentProcessedEventDTO event = new PaymentProcessedEventDTO(
                paymentId, "PIX", new BigDecimal("42.50"), "PENDING", timestamp, timestamp);

        adapter.onPaymentProcessed(event);

        verify(useCase).process(any(UUID.class), eq(1L), any(PaymentVO.class));
    }
}
