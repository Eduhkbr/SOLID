package br.com.fiap.ReceiptApi.domain.factory;

import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.domain.model.Receipt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReceiptFactory {

    public Receipt createFromPayment(PaymentVO paymentVO) {
        String receiptData = generateReceiptData(paymentVO);

        return new Receipt(
                paymentVO.id(),
                receiptData,
                LocalDateTime.now()
        );
    }

    private String generateReceiptData(PaymentVO paymentVO) {
        return String.format(
                "COMPROVANTE DE PAGAMENTO\n" +
                        "ID: %s\n" +
                        "Tipo: %s\n" +
                        "Valor: R$ %.2f\n" +
                        "Data: %s\n" +
                        "Status: %s",
                paymentVO.id(),
                paymentVO.paymentMethod(),
                paymentVO.amount(),
                LocalDateTime.now(),
                paymentVO.status()
        );
    }
}