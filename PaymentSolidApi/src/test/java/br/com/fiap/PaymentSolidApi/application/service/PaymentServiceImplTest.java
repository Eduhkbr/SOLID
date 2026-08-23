package br.com.fiap.PaymentSolidApi.application.service;

import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentNotFoundException;
import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentEventPublisherPort;
import br.com.fiap.PaymentSolidApi.application.port.out.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private PaymentRepository repository;
    private PaymentEventPublisherPort publisher;
    private PaymentServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(PaymentRepository.class);
        publisher = Mockito.mock(PaymentEventPublisherPort.class);
        service = new PaymentServiceImpl(repository, publisher);
    }

    @Test
    void create_saves_and_publishes() {
        Payment payment = Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("12.00"), null, "1111222233334444", "123");
        when(repository.save(any())).thenReturn(payment);

        Payment result = service.create(payment);

        assertThat(result).isEqualTo(payment);
        verify(repository, times(1)).save(payment);
        verify(publisher, times(1)).publishPaymentProcessedEvent(payment);
        verify(publisher, never()).publishPaymentRefundedEvent(any());
    }

    @Test
    void refund_payment_success() {
        UUID id = UUID.randomUUID();
        Payment payment = Payment.create(Payment.PaymentMethod.CREDIT_CARD, new BigDecimal("20"), null, "1111222233334444", "123");
        when(repository.findById(id)).thenReturn(Optional.of(payment));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment refunded = service.refundPayment(id);

        assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(refunded);
        verify(publisher, times(1)).publishPaymentRefundedEvent(refunded);
    }

    @Test
    void refund_not_found_throws() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refundPayment(id))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any());
        verify(publisher, never()).publishPaymentRefundedEvent(any());
    }
}
