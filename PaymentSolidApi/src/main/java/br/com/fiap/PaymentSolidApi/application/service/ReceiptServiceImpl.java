package br.com.fiap.PaymentSolidApi.application.service;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;
import br.com.fiap.PaymentSolidApi.application.port.in.ReceiptService;
import br.com.fiap.PaymentSolidApi.application.port.out.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptServiceImpl.class);
    private final ReceiptRepository receiptRepository;

    public ReceiptServiceImpl(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Override
    public Receipt create(Receipt receipt) {
        return receiptRepository.save(receipt);
    }

    @Override
    public Optional<Receipt> findByPaymentId(UUID paymentId) {
        return receiptRepository.findByPaymentId(paymentId);
    }

    @Override
    public void updateForRefund(Payment refundedPayment) {
        Optional<Receipt> receiptOpt = receiptRepository.findByPaymentId(refundedPayment.getId());

        if (receiptOpt.isPresent()) {
            Receipt receipt = receiptOpt.get();
            receipt.updateForRefund(refundedPayment);
            receiptRepository.save(receipt);
            logger.info("Comprovante {} atualizado para refletir o estorno.", receipt.getPaymentId());
        } else {
            logger.warn("Nenhum comprovante encontrado para o pagamento estornado {}. Nenhuma ação foi tomada.", refundedPayment.getId());
        }
    }
}