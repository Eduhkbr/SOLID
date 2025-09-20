package br.com.fiap.ReceiptApi.domain.vo;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentVO(
        UUID id,
        String paymentMethod,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}