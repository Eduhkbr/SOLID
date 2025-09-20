package br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Dados do comprovante de pagamento
 */

@Schema(name = "ReceiptResponseDTO", description = "Dados do comprovante de pagamento")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-14T12:27:25.065116700-03:00[America/Sao_Paulo]", comments = "Generator version: 7.13.0")
public class ReceiptResponseDTO {

  private @Nullable UUID paymentId;

  private @Nullable String receiptData;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  public ReceiptResponseDTO paymentId(UUID paymentId) {
    this.paymentId = paymentId;
    return this;
  }

  /**
   * ID do pagamento associado a este comprovante
   * @return paymentId
   */
  @Valid 
  @Schema(name = "paymentId", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef", description = "ID do pagamento associado a este comprovante", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("paymentId")
  public UUID getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(UUID paymentId) {
    this.paymentId = paymentId;
  }

  public ReceiptResponseDTO receiptData(String receiptData) {
    this.receiptData = receiptData;
    return this;
  }

  /**
   * Conteúdo do comprovante, geralmente em formato de texto
   * @return receiptData
   */
  
  @Schema(name = "receiptData", example = "COMPROVANTE DE PAGAMENTO ID: a1b2c3d4-e5f6-7890-1234-567890abcdef Tipo: CREDIT_CARD Valor: R$ 150,75 Data: 2025-09-13T18:45:00 Status: APROVADO ", description = "Conteúdo do comprovante, geralmente em formato de texto", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("receiptData")
  public String getReceiptData() {
    return receiptData;
  }

  public void setReceiptData(String receiptData) {
    this.receiptData = receiptData;
  }

  public ReceiptResponseDTO createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Data e hora em que o comprovante foi gerado
   * @return createdAt
   */
  @Valid 
  @Schema(name = "createdAt", description = "Data e hora em que o comprovante foi gerado", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReceiptResponseDTO receiptResponseDTO = (ReceiptResponseDTO) o;
    return Objects.equals(this.paymentId, receiptResponseDTO.paymentId) &&
        Objects.equals(this.receiptData, receiptResponseDTO.receiptData) &&
        Objects.equals(this.createdAt, receiptResponseDTO.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paymentId, receiptData, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReceiptResponseDTO {\n");
    sb.append("    paymentId: ").append(toIndentedString(paymentId)).append("\n");
    sb.append("    receiptData: ").append(toIndentedString(receiptData)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

