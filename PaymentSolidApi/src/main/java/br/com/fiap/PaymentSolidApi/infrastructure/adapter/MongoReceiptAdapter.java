package br.com.fiap.PaymentSolidApi.infrastructure.adapter;

import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.ReceiptMapper;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptMongoEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.mongo.ReceiptMongoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("mongoReceiptAdapter")
public class MongoReceiptAdapter {

    private final ReceiptMongoRepository mongoRepository;

    public MongoReceiptAdapter(ReceiptMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    public Receipt create(Receipt receipt) {
        ReceiptMongoEntity entity = ReceiptMapper.toMongoEntity(receipt);
        ReceiptMongoEntity savedEntity = mongoRepository.save(entity);
        return ReceiptMapper.toDomain(savedEntity);
    }

    public Optional<Receipt> findByPaymentId(UUID paymentId) {
        return mongoRepository.findById(paymentId).map(ReceiptMapper::toDomain);
    }
}
