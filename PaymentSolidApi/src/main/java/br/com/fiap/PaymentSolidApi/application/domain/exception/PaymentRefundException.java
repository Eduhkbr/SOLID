package br.com.fiap.PaymentSolidApi.application.domain.exception;

public class PaymentRefundException extends RuntimeException {

    public PaymentRefundException(String message) {
        super(message);
    }
}