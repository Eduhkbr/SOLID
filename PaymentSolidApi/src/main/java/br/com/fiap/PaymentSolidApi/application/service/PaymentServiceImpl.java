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

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisherPort paymentEventPublisher;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentEventPublisherPort paymentEventPublisher) {
        this.paymentEventPublisher = paymentEventPublisher;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment create(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);

        paymentEventPublisher.publishPaymentProcessedEvent(savedPayment);

        return savedPayment;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    @Override
    @Transactional
    public Payment refundPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id.toString()));

        payment.refund();

        return paymentRepository.save(payment);
    }
}
