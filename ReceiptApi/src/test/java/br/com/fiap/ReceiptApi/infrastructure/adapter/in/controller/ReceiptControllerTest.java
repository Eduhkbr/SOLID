package br.com.fiap.ReceiptApi.infrastructure.adapter.in.controller;

import br.com.fiap.ReceiptApi.application.domain.exception.ReceiptNotFoundException;
import br.com.fiap.ReceiptApi.application.port.in.ReceiptService;
import br.com.fiap.ReceiptApi.domain.model.Receipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReceiptControllerTest {

    private ReceiptService receiptService;
    private ReceiptController controller;

    @BeforeEach
    void setup() {
        receiptService = mock(ReceiptService.class);
        controller = new ReceiptController(receiptService);
    }

    @Test
    void findReceiptByPaymentId_returns200_whenFound() {
        UUID paymentId = UUID.randomUUID();
        Receipt receipt = new Receipt(paymentId, "COMPROVANTE DE PAGAMENTO", LocalDateTime.now());
        when(receiptService.findByPaymentId(paymentId)).thenReturn(Optional.of(receipt));

        ResponseEntity<?> response = controller.findReceiptByPaymentId(paymentId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void findReceiptByPaymentId_throws404_whenNotFound() {
        UUID paymentId = UUID.randomUUID();
        when(receiptService.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.findReceiptByPaymentId(paymentId))
                .isInstanceOf(ReceiptNotFoundException.class)
                .hasMessageContaining(paymentId.toString());
    }
}
