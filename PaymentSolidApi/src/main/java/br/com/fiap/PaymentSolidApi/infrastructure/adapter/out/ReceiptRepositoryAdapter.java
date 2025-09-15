package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.receipt.ReceiptStrategyFactory;

import java.util.Optional;
import java.util.UUID;

public class ReceiptRepositoryAdapter {
    private final ReceiptStrategyFactory strategyFactory;

    public ReceiptRepositoryAdapter(ReceiptStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public void saveReceipt(ReceiptJpaEntity receipt) {
        strategyFactory.getStrategy().saveReceipt(receipt);
    }

    public Optional<ReceiptJpaEntity> findById(UUID id) {
        return strategyFactory.getStrategy().findById(id);
    }
}

