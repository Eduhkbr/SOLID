package br.com.fiap.PaymentSolidApi.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados retornados após o processamento de um pagamento")
public class PaymentResponseDTO {

    @Schema(description = "ID único do pagamento gerado", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID paymentId;

    @Schema(description = "Status atual do pagamento", example = "APPROVED")
    private String status;

    @Schema(description = "Método de pagamento utilizado", example = "CREDIT_CARD")
    private String paymentMethod;

    @Schema(description = "Valor do pagamento processado", example = "150.75")
    private BigDecimal amount;

    @Schema(description = "Mensagem de confirmação", example = "Pagamento processado com sucesso.")
    private String message;

    @Schema(description = "Data e hora em que o pagamento foi processado")
    private LocalDateTime processedAt;
}