package br.com.fiap.ReceiptApi.application.service;

import br.com.fiap.ReceiptApi.application.port.out.ReceiptRepository;
import br.com.fiap.ReceiptApi.domain.factory.ReceiptFactory;
import br.com.fiap.ReceiptApi.domain.model.Receipt;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReceiptServiceImplTest {

    @Test
    void duplicatePaymentEventDoesNotCreateAnotherReceipt() {
        ReceiptRepository repository = mock(ReceiptRepository.class);
        ReceiptFactory factory = mock(ReceiptFactory.class);
        ReceiptServiceImpl service = new ReceiptServiceImpl(repository, factory);
        UUID paymentId = UUID.randomUUID();
        PaymentVO payment = payment(paymentId);
        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.of(new Receipt()));

        service.createReceiptFromPaymentInfo(payment);

        verify(repository, never()).create(any());
        verifyNoInteractions(factory);
    }

    @Test
    void newPaymentEventCreatesReceipt() {
        ReceiptRepository repository = mock(ReceiptRepository.class);
        ReceiptFactory factory = mock(ReceiptFactory.class);
        ReceiptServiceImpl service = new ReceiptServiceImpl(repository, factory);
        UUID paymentId = UUID.randomUUID();
        PaymentVO payment = payment(paymentId);
        Receipt receipt = new Receipt(paymentId, "receipt", payment.createdAt());
        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.empty());
        when(factory.createFromPayment(payment)).thenReturn(receipt);

        service.createReceiptFromPaymentInfo(payment);

        verify(repository).create(receipt);
    }

    @Test
    void refundEventArrivingBeforePaymentEventCreatesFinalReceiptState() {
        ReceiptRepository repository = mock(ReceiptRepository.class);
        ReceiptFactory factory = new ReceiptFactory();
        ReceiptServiceImpl service = new ReceiptServiceImpl(repository, factory);
        UUID paymentId = UUID.randomUUID();
        PaymentVO refundedPayment = payment(paymentId, "REFUNDED");
        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        service.updateForRefund(refundedPayment);

        org.mockito.ArgumentCaptor<Receipt> receiptCaptor = org.mockito.ArgumentCaptor.forClass(Receipt.class);
        verify(repository).create(receiptCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(receiptCaptor.getValue().getReceiptData())
                .contains("Status: REFUNDED", "--- ESTORNO REALIZADO ---");
    }

    private PaymentVO payment(UUID paymentId) {
        return payment(paymentId, "PENDING");
    }

    private PaymentVO payment(UUID paymentId, String status) {
        LocalDateTime now = LocalDateTime.now();
        return new PaymentVO(paymentId, "CREDIT_CARD", new BigDecimal("42.50"), status, now, now);
    }
}
