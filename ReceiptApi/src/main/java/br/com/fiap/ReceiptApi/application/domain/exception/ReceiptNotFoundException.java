package br.com.fiap.ReceiptApi.application.domain.exception;

import java.util.UUID;

public class ReceiptNotFoundException extends RuntimeException {

    public ReceiptNotFoundException(UUID paymentId) {
        super("Comprovante não encontrado para o pagamento: " + paymentId);
    }
}
