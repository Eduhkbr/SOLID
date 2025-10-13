package br.com.fiap.PaymentSolidApi.application.service;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentNotFoundException;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentEventPublisherPort;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisherPort paymentEventPublisher;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentEventPublisherPort paymentEventPublisher) {
        this.paymentEventPublisher = paymentEventPublisher;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment create(Payment payment) {
        logger.info("Iniciando criação de pagamento: {}", payment);
        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Pagamento criado com sucesso: {}", savedPayment.getId());
        paymentEventPublisher.publishPaymentProcessedEvent(savedPayment);
        return savedPayment;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        logger.info("Buscando pagamento por ID: {}", id);
        Optional<Payment> result = paymentRepository.findById(id);
        if (result.isPresent()) {
            logger.info("Pagamento encontrado: {}", id);
        } else {
            logger.warn("Pagamento não encontrado: {}", id);
        }
        return result;
    }

    @Override
    @Transactional
    public Payment refundPayment(UUID id) {
        logger.info("Iniciando estorno de pagamento: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id.toString()));
        payment.refund();
        Payment refunded = paymentRepository.save(payment);
        logger.info("Pagamento estornado com sucesso: {}", id);
        return refunded;
    }
}
