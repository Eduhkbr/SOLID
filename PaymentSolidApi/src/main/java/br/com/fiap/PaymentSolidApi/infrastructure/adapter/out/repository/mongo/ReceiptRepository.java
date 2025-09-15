package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.mongo;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReceiptRepository extends MongoRepository<ReceiptJpaEntity, UUID> {
}

