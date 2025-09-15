package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.PaymentJpaEntity;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.PaymentResponseDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentMapper {

    public static Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) return null;
        return Payment.builder()
                .id(entity.getId())
                .paymentMethod(mapToDomainMethod(entity.getPaymentMethod()))
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getCreatedAt())
                .build();
    }

    public static PaymentJpaEntity toEntity(Payment payment) {
        if (payment == null) return null;
        return PaymentJpaEntity.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    public static PaymentResponseDTO toResponseDto(Payment payment) {
        if (payment == null) return null;
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(payment.getId());
        if (payment.getPaymentMethod() != null)
            dto.setPaymentMethod(PaymentResponseDTO.PaymentMethodEnum.valueOf(payment.getPaymentMethod().name()));
        dto.setAmount(payment.getAmount());
        if (payment.getStatus() != null)
            dto.setStatus(PaymentResponseDTO.StatusEnum.valueOf(payment.getStatus().name()));
        if (payment.getCreatedAt() != null)
            dto.setProcessedAt(payment.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime());
        return dto;
    }

    public static Payment fromRequestDto(PaymentRequestDTO dto) {
        if (dto == null) return null;
        Payment.PaymentMethod method = null;
        if (dto.getPaymentMethod() != null) {
            switch (dto.getPaymentMethod().getValue()) {
                case "CREDIT_CARD" -> method = Payment.PaymentMethod.CREDIT_CARD;
                case "PIX" -> method = Payment.PaymentMethod.PIX;
                case "BOLETO" -> method = Payment.PaymentMethod.BOLETO;
            }
        }
        return Payment.builder()
                .id(UUID.randomUUID())
                .paymentMethod(method)
                .amount(dto.getAmount())
                .pixKey(dto.getPixKey())
                .cardNumber(dto.getCardNumber())
                .cvv(dto.getCvv())
                .status(br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static Payment.PaymentMethod mapToDomainMethod(String method) {
        if (method == null) return null;
        return switch (method) {
            case "CREDIT_CARD" -> Payment.PaymentMethod.CREDIT_CARD;
            case "PIX" -> Payment.PaymentMethod.PIX;
            case "BOLETO" -> Payment.PaymentMethod.BOLETO;
            default -> null;
        };
    }
}

