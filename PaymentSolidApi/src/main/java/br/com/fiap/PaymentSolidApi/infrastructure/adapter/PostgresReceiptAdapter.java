package br.com.fiap.PaymentSolidApi.infrastructure.adapter;

import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.ReceiptMapper;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.jpa.PostgresReceiptJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("postgresReceiptAdapter")
public class PostgresReceiptAdapter {
    private final PostgresReceiptJpaRepository jpaRepository;

    public PostgresReceiptAdapter(PostgresReceiptJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public Receipt create(Receipt receipt) {
        ReceiptJpaEntity entity = ReceiptMapper.toJpaEntity(receipt);
        ReceiptJpaEntity savedEntity = jpaRepository.save(entity);
        return ReceiptMapper.toDomain(savedEntity);
    }

    public Optional<Receipt> findByPaymentId(UUID paymentId) {
        return jpaRepository.findById(paymentId)
                .map(ReceiptMapper::toDomain);
    }
}

