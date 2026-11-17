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

        // O banco usa NUMERIC(19,2): rejeitar frações e overflow antes de persistir,
        // evitando que valor no evento e valor salvo sejam diferentes por arredondamento.
        if (payment.getAmount() != null && (payment.getAmount().stripTrailingZeros().scale() > 2
                || payment.getAmount().compareTo(new java.math.BigDecimal("99999999999999999.99")) > 0)) {
            errors.add("Valor deve ter no máximo duas casas decimais e caber em NUMERIC(19,2).");
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
        if (payment.getCvv() == null || !payment.getCvv().matches("\\d{3,4}")) {
            errors.add("CVV do cartão inválido.");
        }
        if (payment.getPixKey() != null && !payment.getPixKey().isBlank()) {
            errors.add("Chave PIX não deve ser incluída para pagamento por cartão.");
        }
    }

    private void validatePix(Payment payment, List<String> errors) {
        if (payment.getCvv() != null && !payment.getCvv().isBlank()) {
            errors.add("CVV não deve ser incluído para pagamento por PIX.");
        }
        if (payment.getPixKey() != null && payment.getPixKey().length() > 254) {
            errors.add("Chave PIX deve ter no máximo 254 caracteres.");
        }
        if (payment.getPixKey() == null || payment.getPixKey().isBlank()) {
            errors.add("Chave PIX é obrigatória.");
        }
        if (payment.getCardNumber() != null && !payment.getCardNumber().isBlank()) {
            errors.add("Número do cartão não deve ser incluído para pagamento por PIX.");
        }
    }

    private void validateBoleto(Payment payment, List<String> errors) {
        if ((payment.getCardNumber() != null && !payment.getCardNumber().isBlank())
                || (payment.getCvv() != null && !payment.getCvv().isBlank())) {
            errors.add("Dados de cartão não devem ser incluídos para pagamento por boleto.");
        }
        if (payment.getPixKey() != null && !payment.getPixKey().isBlank()) {
            errors.add("Chave PIX não deve ser incluída para pagamento por boleto.");
        }
    }
}
