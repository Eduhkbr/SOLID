package br.com.fiap.PaymentSolidApi.application.port.out;

import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository {

    Receipt save(Receipt receipt);

    Optional<Receipt> findByPaymentId(UUID paymentId);

}
