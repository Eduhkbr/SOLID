package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.receipt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReceiptStrategyFactory {
    private final MongoReceiptStrategy mongoStrategy;
    private final PostgresReceiptStrategy postgresStrategy;
    private final String receiptDbType;

    public ReceiptStrategyFactory(MongoReceiptStrategy mongoStrategy, PostgresReceiptStrategy postgresStrategy,
                                  @Value("${receipt.db.type:mongodb}") String receiptDbType) {
        this.mongoStrategy = mongoStrategy;
        this.postgresStrategy = postgresStrategy;
        this.receiptDbType = receiptDbType;
    }

    public ReceiptStrategy getStrategy() {
        return switch (receiptDbType.toLowerCase()) {
            case "postgres", "postgresql" -> postgresStrategy;
            case "mongodb", "mongo" -> mongoStrategy;
            default -> mongoStrategy;
        };
    }
}

