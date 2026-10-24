package br.com.fiap.PaymentSolidApi.application.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRefundExceptionTest {
    @Test
    void testMessage() {
        PaymentRefundException ex = new PaymentRefundException("msg");
        assertThat(ex.getMessage()).isEqualTo("msg");
    }
}
