package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging.dto;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que representa o evento de um pagamento processado.
 * Este é o contrato de dados que será enviado para a fila.
 * É um objeto anêmico por design, contendo apenas os dados necessários
 * para o contexto de recebimento (ReceiptApi) criar um comprovante.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEventDTO implements Serializable {

    private UUID paymentId;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private LocalDateTime processedAt;

    /**
     * Construtor que atua como um Mapper.
     * Ele converte o objeto de domínio 'Payment' para este DTO,
     * garantindo que apenas os dados necessários para o evento sejam expostos.
     */
    public PaymentProcessedEventDTO(Payment payment) {
        this.paymentId = payment.getId();
        this.paymentMethod = payment.getPaymentMethod().name();
        this.amount = payment.getAmount();
        this.status = payment.getStatus().name();
        this.processedAt = payment.getUpdatedAt();
    }
}
