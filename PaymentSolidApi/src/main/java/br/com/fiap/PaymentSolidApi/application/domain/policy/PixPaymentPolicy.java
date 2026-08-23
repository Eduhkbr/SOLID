package br.com.fiap.PaymentSolidApi.application.domain.policy;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

import java.util.List;

public class PixPaymentPolicy implements PaymentMethodPolicy {

    @Override
    public Payment.PaymentMethod supportedMethod() {
        return Payment.PaymentMethod.PIX;
    }

    @Override
    public void validateCreation(Payment payment, List<String> errors) {
        if (payment.getPixKey() == null || payment.getPixKey().isBlank()) {
            errors.add("Chave PIX é obrigatória.");
        }
        if (payment.getCardNumber() != null && !payment.getCardNumber().isBlank()) {
            errors.add("Número do cartão não deve ser incluído para pagamento por PIX.");
        }
        if (payment.getCvv() != null && !payment.getCvv().isBlank()) {
            errors.add("CVV não deve ser incluído para pagamento por PIX.");
        }
    }

    @Override
    public void validateRefund(Payment payment, List<String> errors) {
        errors.add("Estorno não permitido: o método de pagamento 'PIX' não suporta esta operação.");
    }
}
