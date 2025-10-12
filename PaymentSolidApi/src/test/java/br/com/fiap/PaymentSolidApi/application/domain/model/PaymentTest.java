package br.com.fiap.PaymentSolidApi.application.domain.model;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentRefundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void createCreditCardPayment_success() {
        Payment payment = Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("100.00"), null, "1234567812345678", "123");

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getCardNumber()).isEqualTo("1234567812345678");
        assertThat(payment.getCvv()).isEqualTo("123");
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getUpdatedAt()).isNotNull();
    }

    @Test
    void createPixPayment_success() {
        Payment payment = Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("50"), "meu-pix", null, null);

        assertThat(payment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.PIX);
        assertThat(payment.getPixKey()).isEqualTo("meu-pix");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void createBoletoPayment_success() {
        Payment payment = Payment.create(Payment.PaymentMethod.BOLETO, new BigDecimal("10"), null, null, null);

        assertThat(payment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.BOLETO);
        assertThat(payment.getPixKey()).isNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void fromState_reconstitute() {
        UUID id = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now();
        Payment payment = Payment.fromState(id, Payment.PaymentMethod.BOLETO, new BigDecimal("20"), PaymentStatus.APPROVED, created, updated);

        assertThat(payment.getId()).isEqualTo(id);
        assertThat(payment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.BOLETO);
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("20"));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getCreatedAt()).isEqualTo(created);
        assertThat(payment.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void refund_success_for_credit_card_pending() {
        Payment payment = Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("100.00"), null, "1234567812345678", "123");

        payment.refund();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getUpdatedAt()).isNotNull();
    }

    @Test
    void refund_fail_for_pix() {
        Payment pixPayment = Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("5"), "pix-key", null, null);

        assertThatThrownBy(pixPayment::refund)
                .isInstanceOf(PaymentRefundException.class)
                .hasMessageContaining("PIX");
    }

    @Test
    void refund_fail_when_status_not_pending() {
        Payment payment = Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("100.00"), null, "1234567812345678", "123");
        payment.setStatus(PaymentStatus.PROCESSING);

        assertThatThrownBy(payment::refund)
                .isInstanceOf(PaymentRefundException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void createPayment_invalid_amount_throws() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("0"), null, "1234567812345678", "123"))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("Valor do pagamento é inválido");
    }

    @Test
    void createCreditCard_invalid_card_cvv_and_pix_present_throws() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("10"), "some-pix", "123", "12"))
                .isInstanceOf(PaymentValidationException.class)
                .satisfies(ex -> {
                    PaymentValidationException pve = (PaymentValidationException) ex;
                    assertThat(pve.getErrors()).anyMatch(s -> s.contains("Número do cartão inválido"));
                    assertThat(pve.getErrors()).anyMatch(s -> s.contains("CVV do cartão inválido"));
                    assertThat(pve.getErrors()).anyMatch(s -> s.contains("Chave PIX não deve ser incluída"));
                });
    }

    @Test
    void createPix_missing_pixKey_throws() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("10"), "", null, null))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("Chave PIX é obrigatória");
    }

    @Test
    void createWithNullPaymentMethod_throws() {
        assertThatThrownBy(() -> Payment.create(null, new BigDecimal("10"), null, null, null))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("Tipo de pagamento é obrigatório");
    }
}
