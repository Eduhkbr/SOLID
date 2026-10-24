package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.PaymentProcessedEventDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class PaymentEventListenerAdapterTest {

    @Test
    void refundedPaymentEventUpdatesExistingReceipt() {
        ReceiptService receiptService = mock(ReceiptService.class);
        PaymentEventListenerAdapter adapter = new PaymentEventListenerAdapter(receiptService);
        UUID paymentId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2025, 2, 3, 10, 15);
        LocalDateTime refundedAt = createdAt.plusDays(1);
        PaymentProcessedEventDTO event = new PaymentProcessedEventDTO(
                paymentId, "CREDIT_CARD", new BigDecimal("42.50"), "REFUNDED", createdAt, refundedAt);

        adapter.onPaymentProcessed(event);

        verify(receiptService).updateForRefund(new PaymentVO(
                paymentId, "CREDIT_CARD", new BigDecimal("42.50"), "REFUNDED", createdAt, refundedAt));
        verify(receiptService, never()).createReceiptFromPaymentInfo(any());
    }

    @Test
    void pendingPaymentEventCreatesReceipt() {
        ReceiptService receiptService = mock(ReceiptService.class);
        PaymentEventListenerAdapter adapter = new PaymentEventListenerAdapter(receiptService);
        UUID paymentId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2025, 2, 3, 10, 15);
        PaymentProcessedEventDTO event = new PaymentProcessedEventDTO(
                paymentId, "PIX", new BigDecimal("42.50"), "PENDING", createdAt, createdAt);

        adapter.onPaymentProcessed(event);

        verify(receiptService).createReceiptFromPaymentInfo(any(PaymentVO.class));
        verify(receiptService, never()).updateForRefund(any());
    }
}
