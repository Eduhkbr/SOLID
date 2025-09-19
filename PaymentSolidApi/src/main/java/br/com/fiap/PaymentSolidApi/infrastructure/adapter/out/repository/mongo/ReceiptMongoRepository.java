package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.mongo;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReceiptMongoRepository extends MongoRepository<ReceiptMongoEntity, UUID> {
}

