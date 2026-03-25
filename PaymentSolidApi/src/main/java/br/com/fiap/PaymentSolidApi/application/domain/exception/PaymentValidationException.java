package br.com.fiap.PaymentSolidApi.application.domain.exception;

import java.util.List;

public class PaymentValidationException extends RuntimeException {
    private final List<String> errors;

    public PaymentValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}