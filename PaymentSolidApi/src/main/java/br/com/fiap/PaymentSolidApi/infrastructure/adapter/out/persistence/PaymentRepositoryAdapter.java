package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentRepository;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.jpa.PaymentJpaRepository;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.mappers.PaymentMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {
    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = PaymentMapper.toEntity(payment);
        // O domínio atribui UUID antes da persistência; por isso, o ID não indica
        // se o Spring Data deve inserir (persist) ou atualizar (merge) a entidade.
        entity.setNew(!jpaRepository.existsById(entity.getId()));
        PaymentJpaEntity saved = jpaRepository.save(entity);
        return PaymentMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(PaymentMapper::toDomain);
    }
}
