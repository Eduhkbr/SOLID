package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out;

import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptMongoEntity;
import org.openapitools.model.ReceiptResponseDTO;

import java.time.ZoneId;

public class ReceiptMapper {

    public static Receipt toDomain(ReceiptJpaEntity entity) {
        if (entity == null) return null;
        return new Receipt(
                entity.getPaymentId(),
                entity.getReceiptData(),
                entity.getCreatedAt()
        );
    }

    public static Receipt toDomain(ReceiptMongoEntity entity) {
        if (entity == null) return null;
        return new Receipt(
                entity.getPaymentId(),
                entity.getReceiptData(),
                entity.getCreatedAt()
        );
    }

    public static ReceiptJpaEntity toJpaEntity(Receipt receipt) {
        if (receipt == null) return null;
        return ReceiptJpaEntity.builder()
                .paymentId(receipt.getPaymentId())
                .receiptData(receipt.getReceiptData())
                .createdAt(receipt.getCreatedAt())
                .build();
    }

    public static ReceiptMongoEntity toMongoEntity(Receipt receipt) {
        if (receipt == null) return null;
        return ReceiptMongoEntity.builder()
                .paymentId(receipt.getPaymentId())
                .receiptData(receipt.getReceiptData())
                .createdAt(receipt.getCreatedAt())
                .build();
    }

    public static ReceiptResponseDTO toResponseDto(Receipt receipt) {
        if (receipt == null) return null;
        ReceiptResponseDTO dto = new ReceiptResponseDTO();
        dto.setPaymentId(receipt.getPaymentId());
        dto.setReceiptData(receipt.getReceiptData());
        if (receipt.getCreatedAt() != null) {
            dto.setCreatedAt(receipt.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        }
        return dto;
    }
}