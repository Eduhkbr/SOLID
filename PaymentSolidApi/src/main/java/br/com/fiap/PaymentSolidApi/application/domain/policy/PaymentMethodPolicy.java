package br.com.fiap.PaymentSolidApi.application.domain.policy;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

import java.util.List;

public interface PaymentMethodPolicy {

    Payment.PaymentMethod supportedMethod();

    void validateCreation(Payment payment, List<String> errors);

    void validateRefund(Payment payment, List<String> errors);
}
