package br.com.fiap.PaymentSolidApi.adapter.in.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados para processar um novo pagamento")
public class PaymentRequestDTO {

    @Schema(description = "Método de pagamento", example = "CREDIT_CARD", required = true)
    private String paymentMethod;

    @Schema(description = "Valor do pagamento", example = "150.75", required = true)
    private BigDecimal amount;

    @Schema(description = "Número do cartão de crédito (necessário para CREDIT_CARD)", example = "1234567890123456")
    private String cardNumber;

    @Schema(description = "CVV do cartão de crédito (necessário para CREDIT_CARD)", example = "123")
    private String cvv;

    @Schema(description = "Chave PIX (necessário para PIX)", example = "email@example.com")
    private String pixKey;
}
