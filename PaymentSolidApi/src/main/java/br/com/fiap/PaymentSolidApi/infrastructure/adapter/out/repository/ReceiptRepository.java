package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ReceiptRepository extends MongoRepository<ReceiptJpaEntity, UUID> {
}
