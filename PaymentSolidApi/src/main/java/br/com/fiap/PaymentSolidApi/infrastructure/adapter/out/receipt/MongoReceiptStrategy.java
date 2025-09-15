package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.receipt;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.mongo.ReceiptRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class MongoReceiptStrategy implements ReceiptStrategy {
    private final ReceiptRepository receiptRepository;

    public MongoReceiptStrategy(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Override
    public void saveReceipt(ReceiptJpaEntity receipt) {
        receiptRepository.save(receipt);
    }

    @Override
    public Optional<ReceiptJpaEntity> findById(UUID id) {
        return receiptRepository.findById(id);
    }
}

