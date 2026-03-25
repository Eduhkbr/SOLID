package br.com.fiap.PaymentSolidApi.application.domain.factory;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReceiptFactory {

    public Receipt createFromPayment(Payment payment) {
        String receiptData = generateReceiptData(payment);

        return new Receipt(
                payment.getId(),
                receiptData,
                LocalDateTime.now()
        );
    }

    private String generateReceiptData(Payment payment) {
        return String.format(
                "COMPROVANTE DE PAGAMENTO\n" +
                        "ID: %s\n" +
                        "Tipo: %s\n" +
                        "Valor: R$ %.2f\n" +
                        "Data: %s\n" +
                        "Status: %s",
                payment.getId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                LocalDateTime.now(),
                payment.getStatus()
        );
    }
}