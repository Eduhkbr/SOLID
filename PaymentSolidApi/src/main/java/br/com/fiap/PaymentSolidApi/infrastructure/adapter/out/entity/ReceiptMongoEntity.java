package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptMongoEntity {
    @Id
    private UUID paymentId;
    private String receiptData;
    private LocalDateTime createdAt;
}

