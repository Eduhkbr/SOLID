package br.com.fiap.PaymentSolidApi.application.port.out;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

public interface PaymentEventPublisherPort {

    // Publica a mudança de estado persistida para os serviços interessados.
    void publishPaymentStateChangedEvent(Payment payment);
}
