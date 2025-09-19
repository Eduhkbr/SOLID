package br.com.fiap.PaymentSolidApi.application.domain.model;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentRefundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa a entidade de domínio Payment, o coração da lógica de negócio.
 * Esta classe é responsável por garantir seu próprio estado e consistência.
 * A criação de novas instâncias é controlada através de um metodo de fábrica estático.
 */
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

    // Construtor privado para forçar o uso do metodo de fábrica.
    private Payment() {}

    /**
     * METODO DE FÁBRICA: O único ponto de entrada para criar um novo pagamento.
     * Garante que todo pagamento seja validado no momento da sua criação
     */
    public static Payment create(PaymentMethod paymentMethod, BigDecimal amount, String pixKey, String cardNumber, String cvv) {
        Payment payment = new Payment();
        payment.id = UUID.randomUUID();
        payment.paymentMethod = paymentMethod;
        payment.amount = amount;
        payment.pixKey = pixKey;
        payment.cardNumber = cardNumber;
        payment.cvv = cvv;
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = LocalDateTime.now();
        payment.updatedAt = LocalDateTime.now();

        payment.validate();

        return payment;
    }

    /**
     * Metodo de Reconstituição: Usado pelos adaptadores (mappers) para recriar um objeto
     * a partir de um estado já persistido (confiável), sem executar as validações de criação novamente.
     */
    public static Payment fromState(UUID id, PaymentMethod paymentMethod, BigDecimal amount, PaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Payment payment = new Payment();
        payment.id = id;
        payment.paymentMethod = paymentMethod;
        payment.amount = amount;
        payment.status = status;
        payment.createdAt = createdAt;
        payment.updatedAt = updatedAt;
        return payment;
    }

    public void refund() {
        isRefundable();

        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    private void isRefundable() {
        if (this.paymentMethod == PaymentMethod.PIX) {
            throw new PaymentRefundException("Estorno não permitido: o método de pagamento 'PIX' não suporta esta operação.");
        }

        if (this.status != PaymentStatus.PENDING) {
            throw new PaymentRefundException("Estorno não permitido: o pagamento deve estar no status 'PENDING', mas está em '" + this.status + "'.");
        }
    }

    /**
     * Ponto de entrada para a validação, delegando para uma classe especialista.
     */
    private void validate() {
        new PaymentValidator().validate(this);
    }

    public UUID getId() { return id; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPixKey() { return pixKey; }
    public String getCardNumber() { return cardNumber; }
    public String getCvv() { return cvv; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}