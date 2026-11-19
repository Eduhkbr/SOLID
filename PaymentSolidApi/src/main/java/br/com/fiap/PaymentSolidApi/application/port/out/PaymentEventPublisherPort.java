package br.com.fiap.PaymentSolidApi.application.port.out;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

public interface PaymentEventPublisherPort {

    // Registra a mudança de estado para publicação confiável após o commit local.
    void publishPaymentStateChangedEvent(Payment payment);
}
