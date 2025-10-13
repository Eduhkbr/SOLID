package br.com.fiap.PaymentSolidApi.application.domain.model;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentValidatorTest {
    @Test
    void validate_throwsException_whenAmountIsNull() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.PIX, null, "pix-key", null, null))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Valor do pagamento");
    }

    @Test
    void validate_throwsException_whenAmountIsNegative() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("-1.00"), "pix-key", null, null))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Valor do pagamento");
    }

    @Test
    void validate_throwsException_whenPaymentMethodIsNull() {
        assertThatThrownBy(() -> Payment.create(null, new BigDecimal("10.00"), null, null, null))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Tipo de pagamento");
    }

    @Test
    void validate_doesNotThrow_whenValidPix() {
        Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("10.00"), "pix-key", null, null);
    }
}