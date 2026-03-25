package br.com.fiap.PaymentSolidApi.application.domain.model;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe especialista com a única responsabilidade (SRP) de validar o estado de um objeto Payment.
 * É um componente do domínio, sem dependências externas.
 */
public class PaymentValidator {

    public void validate(Payment payment) throws PaymentValidationException {
        List<String> errors = new ArrayList<>();

        if (payment.getAmount() == null || payment.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.add("Valor do pagamento é inválido.");
        }

        if (payment.getPaymentMethod() == null) {
            errors.add("Tipo de pagamento é obrigatório.");
            throw new PaymentValidationException(errors);
        }

        switch (payment.getPaymentMethod()) {
            case CREDIT_CARD -> validateCreditCard(payment, errors);
            case PIX -> validatePix(payment, errors);
            case BOLETO -> validateBoleto(payment, errors);
        }

        if (!errors.isEmpty()) {
            throw new PaymentValidationException(errors);
        }
    }

    private void validateCreditCard(Payment payment, List<String> errors) {
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

    private void validatePix(Payment payment, List<String> errors) {
        if (payment.getPixKey() == null || payment.getPixKey().isBlank()) {
            errors.add("Chave PIX é obrigatória.");
        }
        if (payment.getCardNumber() != null && !payment.getCardNumber().isBlank()) {
            errors.add("Número do cartão não deve ser incluído para pagamento por PIX.");
        }
    }

    private void validateBoleto(Payment payment, List<String> errors) {
        if (payment.getPixKey() != null && !payment.getPixKey().isBlank()) {
            errors.add("Chave PIX não deve ser incluída para pagamento por boleto.");
        }
    }
}
