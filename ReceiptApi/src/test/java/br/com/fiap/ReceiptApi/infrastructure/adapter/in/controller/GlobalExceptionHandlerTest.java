package br.com.fiap.ReceiptApi.infrastructure.adapter.in.controller;

import br.com.fiap.ReceiptApi.application.domain.exception.ReceiptNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleReceiptNotFound_returns404() {
        UUID paymentId = UUID.randomUUID();
        ReceiptNotFoundException ex = new ReceiptNotFoundException(paymentId);

        ResponseEntity<Map<String, Object>> response = handler.handleReceiptNotFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("code", "RECEIPT_NOT_FOUND");
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody()).containsKey("traceId");
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("unexpected error");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("code", "INTERNAL_SERVER_ERROR");
        assertThat(response.getBody()).containsKey("traceId");
    }
}
