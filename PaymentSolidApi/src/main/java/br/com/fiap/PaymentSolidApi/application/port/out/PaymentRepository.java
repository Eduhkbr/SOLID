package br.com.fiap.PaymentSolidApi.application.port.out;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    void lock(UUID id);

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);
}
