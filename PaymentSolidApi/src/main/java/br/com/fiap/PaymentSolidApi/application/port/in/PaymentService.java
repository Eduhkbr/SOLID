package br.com.fiap.PaymentSolidApi.application.port.in;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

    Payment create(Payment payment);

    Optional<Payment> findById(UUID id);
}
