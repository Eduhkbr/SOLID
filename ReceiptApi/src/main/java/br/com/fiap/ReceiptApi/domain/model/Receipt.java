package br.com.fiap.ReceiptApi.domain.model;

import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * Comportamento de domínio para atualizar o comprovante para um estado de estorno.
     * Anexa as informações do estorno ao conteúdo original do comprovante.
     *
     * @param refundedPaymentVO O objeto de pagamento que foi estornado.
     */
    public void updateForRefund(PaymentVO refundedPaymentVO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String refundTimestamp = refundedPaymentVO.updatedAt().format(formatter);

        String refundInfo = String.format(
                "\n\n--- ESTORNO REALIZADO ---\n" +
                        "Status: %s\n" +
                        "Data do Estorno: %s",
                refundedPaymentVO.status(),
                refundTimestamp
        );

        this.receiptData += refundInfo;
    }

    // Getters e Setters
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public String getReceiptData() { return receiptData; }
    public void setReceiptData(String receiptData) { this.receiptData = receiptData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

