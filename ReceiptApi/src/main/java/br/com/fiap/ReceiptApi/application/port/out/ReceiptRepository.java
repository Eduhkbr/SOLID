package br.com.fiap.ReceiptApi.application.port.out;

import br.com.fiap.ReceiptApi.domain.model.Receipt;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository {

    Receipt create(Receipt receipt);

    Optional<Receipt> findByPaymentId(UUID paymentId);

}
