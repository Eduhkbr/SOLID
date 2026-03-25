package br.com.fiap.PaymentSolidApi.application.service;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentNotFoundException;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import br.com.fiap.PaymentSolidApi.application.port.in.ReceiptService;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReceiptService receiptService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, ReceiptService receiptService) {
        this.paymentRepository = paymentRepository;
        this.receiptService = receiptService;
    }

    @Override
    public Payment create(Payment payment) {
        return paymentRepository.save(payment);
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

        Payment refundedPayment = paymentRepository.save(payment);
        receiptService.updateForRefund(refundedPayment);

        return paymentRepository.save(payment);
    }
}
