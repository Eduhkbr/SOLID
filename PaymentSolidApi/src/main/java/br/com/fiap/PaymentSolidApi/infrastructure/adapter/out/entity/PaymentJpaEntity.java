package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PAYMENTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentJpaEntity {

    @Id
    private String id;

    @Column(name = "PAYMENT_METHOD", nullable = false)
    private String paymentMethod;

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private PaymentStatus status;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
}
