package br.com.fiap.ReceiptApi.infrastructure.adapter.in.controller;

import br.com.fiap.ReceiptApi.application.domain.exception.ReceiptNotFoundException;
import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.dto.ReceiptResponseDTO;
import br.com.fiap.ReceiptApi.infrastructure.adapter.out.repository.ReceiptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ReceiptResponseDTO> findReceiptByPaymentId(@PathVariable UUID paymentId) {
        logger.info("Buscando comprovante para o pagamento {}", paymentId);
        var receipt = receiptService.findByPaymentId(paymentId)
                .orElseThrow(() -> new ReceiptNotFoundException(paymentId));
        return ResponseEntity.ok(ReceiptMapper.toResponseDto(receipt));
    }
}
