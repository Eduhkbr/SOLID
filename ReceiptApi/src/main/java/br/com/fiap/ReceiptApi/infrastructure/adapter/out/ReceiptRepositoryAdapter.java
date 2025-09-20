package br.com.fiap.ReceiptApi.infrastructure.adapter.out;

import br.com.fiap.ReceiptApi.application.port.out.ReceiptRepository;
import br.com.fiap.ReceiptApi.domain.model.Receipt;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.repository.ReceiptJpaRepository;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.repository.ReceiptMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ReceiptRepositoryAdapter implements ReceiptRepository {
    private final ReceiptJpaRepository jpaRepository;

    public ReceiptRepositoryAdapter(ReceiptJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Receipt create(Receipt receipt) {
        ReceiptJpaEntity entity = ReceiptMapper.toJpaEntity(receipt);
        ReceiptJpaEntity savedEntity = jpaRepository.save(entity);
        return ReceiptMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Receipt> findByPaymentId(UUID paymentId) {
        return jpaRepository.findById(paymentId)
                .map(ReceiptMapper::toDomain);
    }
}

