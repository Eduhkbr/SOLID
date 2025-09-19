package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import java.net.URI;
import java.sql.*;
import java.util.UUID;

import br.com.fiap.PaymentSolidApi.application.domain.factory.ReceiptFactory;
import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import br.com.fiap.PaymentSolidApi.application.port.in.ReceiptService;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.ReceiptMapper;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.mappers.PaymentMapper;
import br.com.fiap.paymentsolidiapi.api.PaymentsApi;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.ReceiptResponseDTO;
import org.openapitools.model.PaymentResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final ReceiptFactory receiptFactory;

    public PaymentController(ReceiptService receiptService, PaymentService paymentService, ReceiptFactory receiptFactory) {
        this.receiptService = receiptService;
        this.paymentService = paymentService;
        this.receiptFactory = receiptFactory;
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        final var createdPayment = paymentService.create(PaymentMapper.fromRequestDto(request));
        final var uri = URI.create("/api/payments/" + createdPayment.getId());

        var receipt = receiptFactory.createFromPayment(createdPayment);
        receiptService.create(receipt);

        return ResponseEntity.created(uri).body(PaymentMapper.toResponseDto(createdPayment));
    }
    
    @Override
    public ResponseEntity<PaymentResponseDTO> findPaymentById(@PathVariable("id") UUID id) {
        var paymentOpt = paymentService.findById(id);
        return paymentOpt.map(payment ->
                ResponseEntity.ok(PaymentMapper.toResponseDto(payment))).orElseGet(() ->
                ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> findReceiptById(@PathVariable("id") UUID id) {
        var receiptOpt = receiptService.findByPaymentId(id);
        return receiptOpt.map(receipt ->
                ResponseEntity.ok(ReceiptMapper.toResponseDto(receipt))).orElseGet(() ->
                ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable("id") UUID id) {
        final var paymentRefunded = paymentService.refundPayment(id);
        return ResponseEntity.ok().body(PaymentMapper.toResponseDto(paymentRefunded));
    }
}