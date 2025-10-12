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

        PaymentJpaEntity e1 = new PaymentJpaEntity(id, "CREDIT_CARD", new BigDecimal("10"), PaymentStatus.PENDING, now, now);
        PaymentJpaEntity e2 = new PaymentJpaEntity(id, "CREDIT_CARD", new BigDecimal("10"), PaymentStatus.PENDING, now, now);

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());

        PaymentJpaEntity e3 = new PaymentJpaEntity(UUID.randomUUID(), "PIX", new BigDecimal("5"), PaymentStatus.APPROVED, now, now);
        assertThat(e1).isNotEqualTo(e3);
    }

    @Test
    void equals_null_and_different_class() {
        PaymentJpaEntity e = new PaymentJpaEntity(UUID.randomUUID(), "BOLETO", new BigDecimal("20"), PaymentStatus.REJECTED, LocalDateTime.now(), LocalDateTime.now());
        assertThat(e).isNotEqualTo(null);
        assertThat(e).isNotEqualTo(new Object());
    }
}

