package br.com.fiap.PaymentSolidApi.application.domain.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String id) {
        super("Pagamento não encontrado para o id: " + id);
    }
}

