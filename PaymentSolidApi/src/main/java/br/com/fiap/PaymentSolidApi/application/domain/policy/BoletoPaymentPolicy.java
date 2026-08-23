package br.com.fiap.PaymentSolidApi.application.domain.policy;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

import java.util.List;

public class BoletoPaymentPolicy implements PaymentMethodPolicy {

    @Override
    public Payment.PaymentMethod supportedMethod() {
        return Payment.PaymentMethod.BOLETO;
    }

    @Override
    public void validateCreation(Payment payment, List<String> errors) {
        if (payment.getPixKey() != null && !payment.getPixKey().isBlank()) {
            errors.add("Chave PIX não deve ser incluída para pagamento por boleto.");
        }
        if (payment.getCardNumber() != null && !payment.getCardNumber().isBlank()) {
            errors.add("Número do cartão não deve ser incluído para pagamento por boleto.");
        }
        if (payment.getCvv() != null && !payment.getCvv().isBlank()) {
            errors.add("CVV não deve ser incluído para pagamento por boleto.");
        }
    }

    @Override
    public void validateRefund(Payment payment, List<String> errors) {
    }
}
