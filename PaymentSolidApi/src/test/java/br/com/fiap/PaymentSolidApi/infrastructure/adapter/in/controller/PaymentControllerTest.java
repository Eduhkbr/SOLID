package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.model.PaymentMethodEnum;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.PaymentResponseDTO;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentControllerTest {
    private PaymentService paymentService;
    private PaymentController controller;

    @BeforeEach
    void setup() {
        paymentService = Mockito.mock(PaymentService.class);
        controller = new PaymentController(paymentService);
    }

    @Test
    void createPayment_returnsCreated() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod(PaymentMethodEnum.CREDIT_CARD);
        request.setCardNumber("1234567812345678");
        request.setCvv("123");

        Payment payment = Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("100.00"), null, "1234567812345678", "123");
        when(paymentService.create(any())).thenReturn(payment);

        ResponseEntity<PaymentResponseDTO> response = controller.createPayment(request);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void findPaymentById_returnsOk_whenFound() {
        UUID id = UUID.randomUUID();
        Payment payment = Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("50.00"), "pix-key", null, null);
        when(paymentService.findById(id)).thenReturn(Optional.of(payment));

        ResponseEntity<PaymentResponseDTO> response = controller.findPaymentById(id);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void findPaymentById_returnsNoContent_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(paymentService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<PaymentResponseDTO> response = controller.findPaymentById(id);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();
    }
}
