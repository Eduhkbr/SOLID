package br.com.fiap.ReceiptApi.domain.factory;

import br.com.fiap.ReceiptApi.domain.model.Receipt;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptFactoryTest {

    private final ReceiptFactory factory = new ReceiptFactory();

    @Test
    void receiptUsesPaymentCreationTimeInReceiptData() {
        LocalDateTime paymentCreatedAt = LocalDateTime.of(2025, 2, 3, 10, 15);
        PaymentVO payment = new PaymentVO(
                UUID.randomUUID(), "PIX", new BigDecimal("42.50"), "PENDING", paymentCreatedAt, paymentCreatedAt);

        Receipt receipt = factory.createFromPayment(payment);

        assertThat(receipt.getReceiptData()).contains("Data: " + paymentCreatedAt);
        assertThat(receipt.getCreatedAt()).isNotNull();
    }
}
