package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.mappers;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.junit.jupiter.api.Test;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.PaymentResponseDTO;
import org.openapitools.model.PaymentMethodEnum;
import org.openapitools.model.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    @Test
    void fromRequestDto_null_returns_null() {
        assertThat(PaymentMapper.fromRequestDto(null)).isNull();
    }

    @Test
    void fromRequestDto_maps_credit_card() {
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setPaymentMethod(PaymentMethodEnum.CREDIT_CARD);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setCardNumber("1234567812345678");
        dto.setCvv("123");

        Payment payment = PaymentMapper.fromRequestDto(dto);

        assertThat(payment).isNotNull();
        assertThat(payment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(payment.getCardNumber()).isEqualTo("1234567812345678");
        assertThat(payment.getCvv()).isEqualTo("123");
    }

    @Test
    void toResponseDto_null_returns_null() {
        assertThat(PaymentMapper.toResponseDto(null)).isNull();
    }

    @Test
    void toResponseDto_and_entity_mappings() {
        Payment payment = Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("55.50"), null, "1111222233334444", "321");

        PaymentResponseDTO dto = PaymentMapper.toResponseDto(payment);

        assertThat(dto).isNotNull();
        assertThat(dto.getPaymentId()).isEqualTo(payment.getId());
        assertThat(dto.getAmount()).isEqualByComparingTo(payment.getAmount());
        assertThat(dto.getPaymentMethod().toString()).isEqualTo(payment.getPaymentMethod().name());
        assertThat(dto.getStatus().toString()).isEqualTo(payment.getStatus().name());

        // entity mapping
        var entity = PaymentMapper.toEntity(payment);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(payment.getId());
        assertThat(entity.getPaymentMethod()).isEqualTo(payment.getPaymentMethod().name());
        assertThat(entity.getAmount()).isEqualByComparingTo(payment.getAmount());

        // toDomain from entity
        LocalDateTime created = LocalDateTime.now().minusDays(2);
        LocalDateTime updated = LocalDateTime.now().minusDays(1);
        PaymentJpaEntity jpa = new PaymentJpaEntity(payment.getId(), payment.getPaymentMethod().name(), payment.getAmount(), PaymentStatus.APPROVED, created, updated);

        Payment domain = PaymentMapper.toDomain(jpa);
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(jpa.getId());
        assertThat(domain.getPaymentMethod().name()).isEqualTo(jpa.getPaymentMethod());
        assertThat(domain.getAmount()).isEqualByComparingTo(jpa.getAmount());
        assertThat(domain.getStatus()).isEqualTo(jpa.getStatus());
        assertThat(domain.getCreatedAt()).isEqualTo(created);
        assertThat(domain.getUpdatedAt()).isEqualTo(updated);
    }
}

