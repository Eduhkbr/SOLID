package br.com.fiap.PaymentSolidApi.application.domain.model;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private UUID id;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private String pixKey;
    private String cardNumber;
    private String cvv;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        CREDIT_CARD, PIX, BOLETO
    }

    public Payment(PaymentMethod paymentMethod, BigDecimal amount, String cardNumber, String cvv) {
        this.id = UUID.randomUUID();
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Payment(PaymentMethod paymentMethod, BigDecimal amount, String pixKey, String cardNumber, String cvv) {
        this.id = UUID.randomUUID();
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.pixKey = pixKey;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void validate() {
        new PaymentValidator().validate(this);
    }
}
