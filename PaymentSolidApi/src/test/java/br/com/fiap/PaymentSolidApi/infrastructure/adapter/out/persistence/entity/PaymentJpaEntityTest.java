package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentJpaEntityTest {

    @Test
    void equals_and_hashCode_based_on_id() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PaymentJpaEntity e1 = PaymentJpaEntity.builder()
                .id(id)
                .paymentMethod("CREDIT_CARD")
                .amount(new BigDecimal("10"))
                .status(PaymentStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        PaymentJpaEntity e2 = PaymentJpaEntity.builder()
                .id(id)
                .paymentMethod("CREDIT_CARD")
                .amount(new BigDecimal("10"))
                .status(PaymentStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());

        PaymentJpaEntity e3 = PaymentJpaEntity.builder()
                .id(UUID.randomUUID())
                .paymentMethod("PIX")
                .amount(new BigDecimal("5"))
                .status(PaymentStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(e1).isNotEqualTo(e3);
    }

    @Test
    void equals_null_and_different_class() {
        PaymentJpaEntity e = PaymentJpaEntity.builder()
                .id(UUID.randomUUID())
                .paymentMethod("BOLETO")
                .amount(new BigDecimal("20"))
                .status(PaymentStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(e).isNotEqualTo(null);
        assertThat(e).isNotEqualTo(new Object());
    }
}

