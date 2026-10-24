package br.com.fiap.PaymentSolidApi.application.domain.exception;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentValidationExceptionTest {
    @Test
    void testErrors() {
        PaymentValidationException ex = new PaymentValidationException(List.of("erro1", "erro2"));
        assertThat(ex.getErrors()).containsExactly("erro1", "erro2");
    }
}
