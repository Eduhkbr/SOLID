package br.com.fiap.PaymentSolidApi.application.domain.model;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
class PaymentPrecisionTest {
    @ParameterizedTest @ValueSource(strings={"0","-1","0.001","1.999","100000000000000000.00"})
    void rejectsAmountsThatCannotBeStoredExactly(String amount) {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.BOLETO,new BigDecimal(amount),null,null,null))
            .isInstanceOf(PaymentValidationException.class);
    }
    @ParameterizedTest @ValueSource(strings={"0.01","1.000","99999999999999999.99"})
    void acceptsExactlyRepresentableAmounts(String amount) {
        assertThat(Payment.create(Payment.PaymentMethod.BOLETO,new BigDecimal(amount),null,null,null).getAmount()).isEqualByComparingTo(amount);
    }
    @Test void acceptsFourDigitCvvDeclaredByContract() {
        assertThatCode(() -> Payment.create(Payment.PaymentMethod.CREDIT_CARD,BigDecimal.TEN,null,"1234567890123456","1234")).doesNotThrowAnyException();
    }
    @Test void rejectsCardDataForBoletoAndPix() {
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.BOLETO,BigDecimal.TEN,null,"1234567890123456","123")).isInstanceOf(PaymentValidationException.class);
        assertThatThrownBy(() -> Payment.create(Payment.PaymentMethod.PIX,BigDecimal.TEN,"ficticia",null,"123")).isInstanceOf(PaymentValidationException.class);
    }
}
