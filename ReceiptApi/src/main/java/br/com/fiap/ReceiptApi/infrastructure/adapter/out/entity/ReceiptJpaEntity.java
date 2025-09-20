package br.com.fiap.ReceiptApi.infrastructure.adapter.out.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PAYMENT_RECEIPTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptJpaEntity {
    @Id
    @Column(name = "PAYMENT_ID")
    private UUID paymentId;

    @Column(name = "RECEIPT_DATA")
    private String receiptData;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
