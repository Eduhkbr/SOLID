package br.com.fiap.ReceiptApi.application.service;

import br.com.fiap.ReceiptApi.application.port.out.ReceiptRepository;
import br.com.fiap.ReceiptApi.domain.factory.ReceiptFactory;
import br.com.fiap.ReceiptApi.domain.model.Receipt;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReceiptServiceImplTest {

    private ReceiptRepository receiptRepository;
    private ReceiptFactory receiptFactory;
    private ReceiptServiceImpl receiptService;

    @BeforeEach
    void setup() {
        receiptRepository = mock(ReceiptRepository.class);
        receiptFactory = mock(ReceiptFactory.class);
        receiptService = new ReceiptServiceImpl(receiptRepository, receiptFactory);
    }

    @Test
    void createReceiptFromPaymentInfo_createsAndPersistsReceipt() {
        PaymentVO payment = new PaymentVO(
                UUID.randomUUID(),
                "CREDIT_CARD",
                new BigDecimal("42.50"),
                "PENDING",
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now()
        );
        Receipt receipt = new Receipt(payment.id(), "receipt-data", LocalDateTime.now());
        when(receiptFactory.createFromPayment(payment)).thenReturn(receipt);
        when(receiptRepository.create(receipt)).thenReturn(receipt);

        receiptService.createReceiptFromPaymentInfo(payment);

        verify(receiptFactory, times(1)).createFromPayment(payment);
        verify(receiptRepository, times(1)).create(receipt);
    }

    @Test
    void updateForRefund_updatesExistingReceipt() {
        UUID paymentId = UUID.randomUUID();
        PaymentVO refundedPayment = new PaymentVO(
                paymentId,
                "CREDIT_CARD",
                new BigDecimal("42.50"),
                "REFUNDED",
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now()
        );
        Receipt existingReceipt = new Receipt(paymentId, "receipt-data", LocalDateTime.now().minusMinutes(1));
        when(receiptRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(existingReceipt));
        when(receiptRepository.create(any(Receipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        receiptService.updateForRefund(refundedPayment);

        ArgumentCaptor<Receipt> captor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository, times(1)).create(captor.capture());
        assertThat(captor.getValue().getReceiptData()).contains("ESTORNO REALIZADO");
    }

    @Test
    void updateForRefund_doesNothingWhenReceiptIsMissing() {
        UUID paymentId = UUID.randomUUID();
        PaymentVO refundedPayment = new PaymentVO(
                paymentId,
                "BOLETO",
                new BigDecimal("10.00"),
                "REFUNDED",
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now()
        );
        when(receiptRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        receiptService.updateForRefund(refundedPayment);

        verify(receiptRepository, never()).create(any());
    }
}
