package br.com.fiap.PaymentSolidApi.application.domain.model;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;

import java.util.ArrayList;
import java.util.List;

public class PaymentValidator {
    public void validate(Payment payment) throws PaymentValidationException {
        List<String> errors = new ArrayList<>();
        if (payment.getAmount() == null || payment.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.add("Valor do pagamento é inválido.");
        }
        if (payment.getPaymentMethod() == null) {
            errors.add("Tipo de pagamento é obrigatório.");
        }
        switch (payment.getPaymentMethod()) {
            case CREDIT_CARD -> {
                if (payment.getCardNumber() == null || payment.getCardNumber().length() != 16) {
                    errors.add("Número do cartão inválido.");
                }
                if (payment.getCvv() == null || payment.getCvv().length() != 3) {
                    errors.add("CVV do cartão inválido.");
                }
                if (payment.getPixKey() != null && !payment.getPixKey().isBlank()) {
                    errors.add("Chave PIX não deve ser incluída para pagamento por cartão.");
                }
            }
            case PIX -> {
                if (payment.getPixKey() == null || payment.getPixKey().isBlank()) {
                    errors.add("Chave PIX é obrigatória.");
                }
                if (payment.getCardNumber() != null && !payment.getCardNumber().isBlank()) {
                    errors.add("Número do cartão não deve ser incluído para pagamento por PIX.");
                }
            }
            case BOLETO -> {
                if (payment.getPixKey() != null && !payment.getPixKey().isBlank()) {
                    errors.add("Chave PIX não deve ser incluída para pagamento por boleto.");
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new PaymentValidationException(errors);
        }
    }
}
