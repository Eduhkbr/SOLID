package br.com.fiap.ReceiptApi.application.port.in;

import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.domain.model.Receipt;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptService {

    void createReceiptFromPaymentInfo(PaymentVO receipt);

    Optional<Receipt> findByPaymentId(UUID paymentId);

    void updateForRefund(PaymentVO refundedPaymentVO);
}
