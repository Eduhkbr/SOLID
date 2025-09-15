package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.receipt;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.jpa.PostgresReceiptJpaRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class PostgresReceiptStrategy implements ReceiptStrategy {
    private final PostgresReceiptJpaRepository repository;

    public PostgresReceiptStrategy(PostgresReceiptJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveReceipt(ReceiptJpaEntity receipt) {
        repository.save(receipt);
    }

    @Override
    public Optional<ReceiptJpaEntity> findById(UUID id) {
        return repository.findById(id);
    }
}
