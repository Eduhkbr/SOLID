package br.com.fiap.PaymentSolidApi.application.domain.model;

import br.com.fiap.PaymentSolidApi.application.domain.policy.PaymentMethodPolicies;
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

        PaymentMethodPolicies.forMethod(payment.getPaymentMethod()).validateCreation(payment, errors);

        if (!errors.isEmpty()) {
            throw new PaymentValidationException(errors);
        }
    }
}
