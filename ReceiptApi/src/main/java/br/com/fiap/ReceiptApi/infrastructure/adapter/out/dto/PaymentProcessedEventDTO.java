package br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto;

import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
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
 * Implementa Serializable para garantir que possa ser convertido em bytes pela mensageria.
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
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    /**
     * Construtor de conveniência que atua como um Mapper.
     * Ele converte o objeto de domínio 'Payment' para este DTO
     */
    public PaymentProcessedEventDTO(PaymentVO payment) {
        this.paymentId = payment.id();
        this.paymentMethod = payment.paymentMethod();
        this.amount = payment.amount();
        this.status = payment.status();
        this.createdAt = payment.createdAt();
        this.processedAt = payment.updatedAt();
    }
}
