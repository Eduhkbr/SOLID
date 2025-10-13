package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;


import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentNotFoundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentRefundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handlePaymentNotFound_returns404() {
        PaymentNotFoundException ex = new PaymentNotFoundException("123");
        ResponseEntity<Map<String, String>> response = handler.handlePaymentNotFound(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("error", "Pagamento não encontrado para o id: 123");
    }

    @Test
    void handlePaymentValidation_returns400() {
        PaymentValidationException ex = mock(PaymentValidationException.class);
        when(ex.getErrors()).thenReturn(Collections.singletonList("Campo obrigatório ausente"));
        ResponseEntity<Map<String, Object>> response = handler.handlePaymentValidation(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "Erro de validação");
        assertThat(response.getBody()).containsKey("details");
    }

    @Test
    void handlePaymentRefundException_returns409() {
        PaymentRefundException ex = new PaymentRefundException("Reembolso não permitido");
        ResponseEntity<Map<String, String>> response = handler.handlePaymentRefundException(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).containsEntry("error", "Conflito ao processar estorno");
        assertThat(response.getBody()).containsEntry("details", "Reembolso não permitido");
    }
}
