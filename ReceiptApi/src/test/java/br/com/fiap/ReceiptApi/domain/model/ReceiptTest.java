package br.com.fiap.ReceiptApi.domain.model;

import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptTest {

    @Test
    void repeatedRefundEventDoesNotAppendDuplicateInformation() {
        UUID paymentId = UUID.randomUUID();
        LocalDateTime refundAt = LocalDateTime.of(2025, 2, 3, 10, 15);
        Receipt receipt = new Receipt(paymentId, "COMPROVANTE DE PAGAMENTO", refundAt);
        PaymentVO refundedPayment = new PaymentVO(
                paymentId, "CREDIT_CARD", new BigDecimal("42.50"), "REFUNDED", refundAt.minusDays(1), refundAt);

        receipt.updateForRefund(refundedPayment);
        String firstUpdate = receipt.getReceiptData();
        receipt.updateForRefund(refundedPayment);

        assertThat(receipt.getReceiptData()).isEqualTo(firstUpdate);
        assertThat(receipt.getReceiptData()).containsOnlyOnce("--- ESTORNO REALIZADO ---");
    }
}
