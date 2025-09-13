package br.com.fiap.PaymentSolidApi.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Dados do comprovante de pagamento")
public class ReceiptResponseDTO {

    @Schema(description = "ID do pagamento associado a este comprovante",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID paymentId;

    @Schema(description = "Conteúdo do comprovante, geralmente em formato de texto ou JSON",
            example = "{\"loja\":\"Minha Loja\",\"valor\":\"R$ 150,75\",\"data\":\"2025-09-13\"}")
    private String receiptData;

    @Schema(description = "Data e hora em que o comprovante foi gerado",
            example = "2025-09-13T18:45:00")
    private LocalDateTime createdAt;
}