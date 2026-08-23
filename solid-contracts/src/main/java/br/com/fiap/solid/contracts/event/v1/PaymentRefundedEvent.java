package br.com.fiap.solid.contracts.event.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentRefundedEvent(
        UUID paymentId,
        String paymentMethod,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime refundedAt
) implements Serializable {
}
