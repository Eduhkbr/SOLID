package br.com.fiap.PaymentSolidApi.application.port.out;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

public interface PaymentEventPublisherPort {

    // Publica um evento indicando que um pagamento foi processado
    void publishPaymentProcessedEvent(Payment payment);
}
