package br.com.fiap.PaymentSolidApi.application.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentNotFoundExceptionTest {
    @Test
    void testMessage() {
        PaymentNotFoundException ex = new PaymentNotFoundException("123");
        assertThat(ex.getMessage()).isEqualTo("Pagamento não encontrado para o id: 123");
    }
}

