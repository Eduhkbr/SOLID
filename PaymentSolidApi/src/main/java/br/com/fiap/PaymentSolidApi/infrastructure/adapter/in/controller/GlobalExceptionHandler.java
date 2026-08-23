package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentNotFoundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentRefundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.MDC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotFound(PaymentNotFoundException ex) {
        logger.warn("Pagamento não encontrado: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentValidation(PaymentValidationException ex) {
        logger.warn("Erro de validação de pagamento: {} - detalhes: {}", ex.getClass().getSimpleName(), ex.getErrors());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "PAYMENT_VALIDATION_ERROR", "Erro de validação", ex.getErrors());
    }

    @ExceptionHandler(PaymentRefundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentRefundException(PaymentRefundException ex) {
        logger.warn("Conflito ao processar estorno: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "PAYMENT_REFUND_CONFLICT", "Conflito ao processar estorno", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        logger.warn("Erro de validação nos parâmetros: {} - detalhes: {}", ex.getClass().getSimpleName(), ex.getBindingResult().getFieldErrors().toString());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR", "Erro de validação nos parâmetros",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                        .toList());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Erro interno do servidor: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Erro interno do servidor", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String code, String message, Object details) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("traceId", resolveTraceId());
        if (details != null) {
            error.put("details", details);
        }
        return ResponseEntity.status(status).body(error);
    }

    private String resolveTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            traceId = MDC.get("X-Request-ID");
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }
}
