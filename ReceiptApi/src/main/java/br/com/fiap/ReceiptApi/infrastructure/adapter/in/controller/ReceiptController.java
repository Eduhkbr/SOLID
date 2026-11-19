package br.com.fiap.ReceiptApi.infrastructure.adapter.in.controller;

import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.ReceiptResponseDTO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.repository.ReceiptMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ReceiptResponseDTO> findReceiptByPaymentId(@PathVariable UUID paymentId) {
        return receiptService.findByPaymentId(paymentId)
                .map(ReceiptMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
