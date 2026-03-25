package br.com.fiap.PaymentSolidApi.application.port.in;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptService {

    Receipt create(Receipt receipt);

    Optional<Receipt> findByPaymentId(UUID paymentId);

    void updateForRefund(Payment refundedPayment);
}
