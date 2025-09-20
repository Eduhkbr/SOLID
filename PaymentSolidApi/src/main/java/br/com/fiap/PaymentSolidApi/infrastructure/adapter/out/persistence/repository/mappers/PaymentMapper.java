package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.mappers;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.PaymentResponseDTO;
import org.openapitools.model.PaymentMethodEnum;
import org.openapitools.model.StatusEnum;

import java.time.ZoneId;

/**
 * Adaptador responsável por traduzir objetos entre as camadas de API, Domínio e Persistência.
 * Segue o padrão da Arquitetura Hexagonal, isolando as dependências de tecnologia do núcleo de negócio.
 */
public class PaymentMapper {

    /**
     * Converte um DTO de requisição para o modelo de domínio.
     * USA O METODO DE FÁBRICA para garantir que o objeto de domínio seja criado em um estado válido.
     */
    public static Payment fromRequestDto(PaymentRequestDTO dto) {
        if (dto == null) return null;

        Payment.PaymentMethod method = null;
        if (dto.getPaymentMethod() != null) {
            method = Payment.PaymentMethod.valueOf(dto.getPaymentMethod().getValue());
        }

        return Payment.create(
                method,
                dto.getAmount(),
                dto.getPixKey(),
                dto.getCardNumber(),
                dto.getCvv()
        );
    }

    /**
     * Converte uma entidade JPA para o modelo de domínio.
     * USA O METODO DE RECONSTITUIÇÃO para recriar o objeto a partir de um estado confiável (banco de dados),
     * sem executar novamente as validações de criação.
     */
    public static Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) return null;

        return Payment.fromState(
                entity.getId(),
                Payment.PaymentMethod.valueOf(entity.getPaymentMethod()),
                entity.getAmount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Converte o modelo de domínio para uma entidade JPA para persistência.
     */
    public static PaymentJpaEntity toEntity(Payment payment) {
        if (payment == null) return null;

        return new PaymentJpaEntity(
                payment.getId(),
                payment.getPaymentMethod().name(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    /**
     * Converte o modelo de domínio para um DTO de resposta para a API.
     */
    public static PaymentResponseDTO toResponseDto(Payment payment) {
        if (payment == null) return null;

        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setProcessedAt(payment.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        dto.setPaymentMethod(PaymentMethodEnum.valueOf(payment.getPaymentMethod().name()));
        dto.setStatus(StatusEnum.valueOf(payment.getStatus().name()));

        return dto;
    }
}
