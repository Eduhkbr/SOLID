package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentNotFoundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentRefundException;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handlePaymentNotFound(PaymentNotFoundException ex) {
        logger.warn("Pagamento não encontrado: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PaymentValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handlePaymentValidation(PaymentValidationException ex) {
        logger.warn("Erro de validação de pagamento: {} - detalhes: {}", ex.getClass().getSimpleName(), ex.getErrors());
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro de validação");
        error.put("details", ex.getErrors());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PaymentRefundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, String>> handlePaymentRefundException(PaymentRefundException ex) {
        logger.warn("Conflito ao processar estorno: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Conflito ao processar estorno");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        logger.warn("Erro de validação nos parâmetros: {} - detalhes: {}", ex.getClass().getSimpleName(), ex.getBindingResult().getFieldErrors().toString());
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Erro de validação nos parâmetros");
        error.put("details", ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Erro interno do servidor: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erro interno do servidor");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
