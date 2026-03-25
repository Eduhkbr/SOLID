package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
