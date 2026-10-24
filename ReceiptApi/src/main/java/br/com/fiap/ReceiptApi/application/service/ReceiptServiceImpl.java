package br.com.fiap.ReceiptApi.application.service;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.application.port.out.ReceiptRepository;
import br.com.fiap.ReceiptApi.domain.factory.ReceiptFactory;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.domain.model.Receipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptServiceImpl.class);

    private final ReceiptRepository receiptRepository;
    private final ReceiptFactory receiptFactory;

    public ReceiptServiceImpl(ReceiptRepository receiptRepository, ReceiptFactory receiptFactory) {
        this.receiptRepository = receiptRepository;
        this.receiptFactory = receiptFactory;
    }


    @Override
    public void createReceiptFromPaymentInfo(PaymentVO vo) {
        if (receiptRepository.findByPaymentId(vo.id()).isPresent()) {
            logger.info("Comprovante já existe para o pagamento {}; evento duplicado ignorado.", vo.id());
            return;
        }

        // Usa a factory para criar o objeto de domínio
        Receipt newReceipt = receiptFactory.createFromPayment(vo);

        // Usa a porta de saída para persistir o objeto
        receiptRepository.create(newReceipt);

        logger.info("Comprovante para o pagamento ID {} salvo com sucesso.", vo.id());
    }

    @Override
    public Optional<Receipt> findByPaymentId(UUID paymentId) {
        return receiptRepository.findByPaymentId(paymentId);
    }

    @Override
    public void updateForRefund(PaymentVO refundedPaymentVO) {
        Optional<Receipt> receiptOpt = receiptRepository.findByPaymentId(refundedPaymentVO.id());

        if (receiptOpt.isPresent()) {
            Receipt receipt = receiptOpt.get();
            receipt.updateForRefund(refundedPaymentVO);
            receiptRepository.create(receipt);
            logger.info("Comprovante {} atualizado para refletir o estorno.", receipt.getPaymentId());
        } else {
            logger.warn("Nenhum comprovante encontrado para o pagamento estornado {}. Nenhuma ação foi tomada.", refundedPaymentVO.id());
        }
    }
}
