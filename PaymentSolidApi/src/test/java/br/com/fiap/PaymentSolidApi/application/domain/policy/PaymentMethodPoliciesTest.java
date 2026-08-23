package br.com.fiap.PaymentSolidApi.application.domain.policy;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentRefundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentMethodPoliciesTest {

    @Test
    void pixPolicy_shouldRejectRefund() {
        Payment payment = Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("10"), "pix-key", null, null);

        assertThatThrownBy(payment::refund)
                .isInstanceOf(PaymentRefundException.class)
                .hasMessageContaining("PIX");
    }

    @Test
    void boletoPolicy_shouldRejectCardData() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.BOLETO, new BigDecimal("10"), null, "1234567812345678", "123"))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("boleto");
    }

    @Test
    void pixPolicy_shouldRejectCvvField() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("10"), "pix-key", null, "123"))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("CVV");
    }
}
