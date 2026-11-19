package br.com.fiap.ReceiptApi.application.service;

import br.com.fiap.ReceiptApi.application.port.in.ProcessPaymentEventUseCase;
import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.application.port.out.ProcessedEventRepository;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessPaymentEventService implements ProcessPaymentEventUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPaymentEventService.class);

    private final ProcessedEventRepository processedEventRepository;
    private final ReceiptService receiptService;

    public ProcessPaymentEventService(ProcessedEventRepository processedEventRepository, ReceiptService receiptService) {
        this.processedEventRepository = processedEventRepository;
        this.receiptService = receiptService;
    }

    @Override
    @Transactional
    public void process(UUID eventId, long aggregateVersion, PaymentVO payment) {
        processedEventRepository.lockPayment(payment.id());
        long latest = processedEventRepository.latestVersion(payment.id());
        if (!processedEventRepository.registerIfAbsent(eventId, payment.id(), aggregateVersion)) {
            logger.info("Evento {} já foi aplicado; reentrega ignorada.", eventId);
            return;
        }

        // Registrar também o evento antigo permite reconhecer sua reentrega, sem regredir estado.
        if (aggregateVersion <= latest) return;
        if ("REFUNDED".equals(payment.status())) {
            receiptService.updateForRefund(payment);
        } else {
            receiptService.createReceiptFromPaymentInfo(payment);
        }
    }
}
