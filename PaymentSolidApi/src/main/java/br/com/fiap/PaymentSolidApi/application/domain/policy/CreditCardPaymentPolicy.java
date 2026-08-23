package br.com.fiap.PaymentSolidApi.application.domain.policy;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

import java.util.List;

public class CreditCardPaymentPolicy implements PaymentMethodPolicy {

    @Override
    public Payment.PaymentMethod supportedMethod() {
        return Payment.PaymentMethod.CREDIT_CARD;
    }

    @Override
    public void validateCreation(Payment payment, List<String> errors) {
        if (payment.getCardNumber() == null || !payment.getCardNumber().matches("\\d{16}")) {
            errors.add("Número do cartão inválido.");
        }
        if (payment.getCvv() == null || !payment.getCvv().matches("\\d{3}")) {
            errors.add("CVV do cartão inválido.");
        }
        if (payment.getPixKey() != null && !payment.getPixKey().isBlank()) {
            errors.add("Chave PIX não deve ser incluída para pagamento por cartão.");
        }
    }

    @Override
    public void validateRefund(Payment payment, List<String> errors) {
    }
}
