package br.com.fiap.ReceiptApi.infrastructure.adapter.in.messaging;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.solid.contracts.event.v1.PaymentProcessedEvent;
import br.com.fiap.solid.contracts.event.v1.PaymentRefundedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentEventListenerAdapterTest {

    private ReceiptService receiptService;
    private PaymentEventListenerAdapter listenerAdapter;

    @BeforeEach
    void setup() {
        receiptService = mock(ReceiptService.class);
        listenerAdapter = new PaymentEventListenerAdapter(receiptService);
    }

    @Test
    void onPaymentProcessed_mapsEventToReceiptCreation() {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                UUID.randomUUID(),
                "PIX",
                new BigDecimal("19.90"),
                "PENDING",
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now()
        );

        listenerAdapter.onPaymentProcessed(event);

        ArgumentCaptor<PaymentVO> captor = ArgumentCaptor.forClass(PaymentVO.class);
        verify(receiptService, times(1)).createReceiptFromPaymentInfo(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(event.paymentId());
        assertThat(captor.getValue().updatedAt()).isEqualTo(event.processedAt());
    }

    @Test
    void onPaymentRefunded_mapsEventToReceiptRefundUpdate() {
        PaymentRefundedEvent event = new PaymentRefundedEvent(
                UUID.randomUUID(),
                "CREDIT_CARD",
                new BigDecimal("100.00"),
                "REFUNDED",
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now()
        );

        listenerAdapter.onPaymentRefunded(event);

        ArgumentCaptor<PaymentVO> captor = ArgumentCaptor.forClass(PaymentVO.class);
        verify(receiptService, times(1)).updateForRefund(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(event.paymentId());
        assertThat(captor.getValue().updatedAt()).isEqualTo(event.refundedAt());
    }
}
