package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import java.net.URI;
import java.util.UUID;

import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.mappers.PaymentMapper;
import br.com.fiap.paymentsolidiapi.api.PaymentsApi;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.PaymentResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        final var createdPayment = paymentService.create(PaymentMapper.fromRequestDto(request));
        final var uri = URI.create("/api/payments/" + createdPayment.getId());

        return ResponseEntity.created(uri).body(PaymentMapper.toResponseDto(createdPayment));
    }
    
    @Override
    public ResponseEntity<PaymentResponseDTO> findPaymentById(@PathVariable("id") UUID id) {
        var paymentOpt = paymentService.findById(id);
        return paymentOpt.map(payment ->
                ResponseEntity.ok(PaymentMapper.toResponseDto(payment))).orElseGet(() ->
                ResponseEntity.noContent().build());
    }

//    @Override
//    public ResponseEntity<ReceiptResponseDTO> findReceiptById(@PathVariable("id") UUID id) {
//        return null;
//    }

    @Override
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable("id") UUID id) {
        final var paymentRefunded = paymentService.refundPayment(id);
        return ResponseEntity.ok().body(PaymentMapper.toResponseDto(paymentRefunded));
    }
}