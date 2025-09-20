package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "PAYMENTS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "PAYMENT_METHOD", nullable = false)
    private String paymentMethod;

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private PaymentStatus status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // A implementação correta de equals e hashCode para entidades JPA
    // se baseia apenas no ID, garantindo consistência com o banco de dados.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentJpaEntity that = (PaymentJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
