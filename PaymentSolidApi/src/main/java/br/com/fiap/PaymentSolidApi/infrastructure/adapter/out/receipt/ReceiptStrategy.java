package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.receipt;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptStrategy {
    void saveReceipt(ReceiptJpaEntity receipt);
    Optional<ReceiptJpaEntity> findById(UUID id);
}

