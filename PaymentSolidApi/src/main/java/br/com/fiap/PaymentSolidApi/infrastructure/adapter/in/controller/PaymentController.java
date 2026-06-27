package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import java.net.InetAddress;
import java.net.URI;
import java.util.UUID;

import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.mappers.PaymentMapper;
import br.com.fiap.paymentsolidiapi.api.PaymentsApi;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.PaymentResponseDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1")
public class PaymentController implements PaymentsApi {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    // Injeta o valor da variável de ambiente 'INSTANCE_ID'.
    // Se não for definida, usa 'instancia-padrao'.
    @Value("${INSTANCE_ID:instancia-padrao}")
    private String instanceId;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        logger.info("Requisição 'createPayment' recebida pela instância: {}", instanceId);

        final var createdPayment = paymentService.create(PaymentMapper.fromRequestDto(request));
        final var uri = URI.create("/api/v1/payments/" + createdPayment.getId());

        return ResponseEntity.created(uri).body(PaymentMapper.toResponseDto(createdPayment));
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> findPaymentById(@PathVariable("id") UUID id) {
        logger.info("Requisição 'findPaymentById' para o ID {} recebida pela instância: {}", id, instanceId);

        var paymentOpt = paymentService.findById(id);
        return paymentOpt.map(payment ->
                ResponseEntity.ok(PaymentMapper.toResponseDto(payment))).orElseGet(() ->
                ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable("id") UUID id) {
        logger.info("Requisição 'refundPayment' para o ID {} recebida pela instância: {}", id, instanceId);

        final var paymentRefunded = paymentService.refundPayment(id);
        return ResponseEntity.ok().body(PaymentMapper.toResponseDto(paymentRefunded));
    }

    @GetMapping("/payments/host")
    public ResponseEntity<String> getHost() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return ResponseEntity.ok("Requisição processada pela instância do container (hostname): " + hostname);
        } catch (Exception e) {
            return ResponseEntity.ok("Não foi possível obter o hostname da instância.");
        }
    }
}