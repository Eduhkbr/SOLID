package br.com.fiap.PaymentSolidApi.application.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Receipt {
    private UUID paymentId;
    private String receiptData;
    private LocalDateTime createdAt;

    public Receipt() {}

    public Receipt(UUID paymentId, String receiptData, LocalDateTime createdAt) {
        this.paymentId = paymentId;
        this.receiptData = receiptData;
        this.createdAt = createdAt;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

