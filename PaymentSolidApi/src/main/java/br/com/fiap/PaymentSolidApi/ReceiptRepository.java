package br.com.fiap.PaymentSolidApi;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ReceiptRepository extends MongoRepository<Receipt, UUID> {
}
