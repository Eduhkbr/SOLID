package br.com.fiap.PaymentSolidApi.application.domain.exception;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;

public class PaymentRefundException extends RuntimeException {

    public PaymentRefundException(PaymentStatus currentStatus) {
        super("O pagamento com status '" + currentStatus + "' não pode ser estornado.");
    }

    public PaymentRefundException(String message) {
        super(message);
    }
}