package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentEventOutboxAdapterTest {

    @Test
    void storesAnIdentifiedVersionedEventInsteadOfPublishingImmediately() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PaymentEventOutboxAdapter adapter = new PaymentEventOutboxAdapter(
                jdbcTemplate, new ObjectMapper().registerModule(new JavaTimeModule()));
        Payment payment = mock(Payment.class);
        UUID paymentId = UUID.randomUUID();
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getPaymentMethod()).thenReturn(Payment.PaymentMethod.CREDIT_CARD);
        when(payment.getAmount()).thenReturn(new BigDecimal("10.00"));
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(payment.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 10, 0));
        when(payment.getUpdatedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 10, 0));

        SpanContext spanContext = SpanContext.create(
                "0123456789abcdef0123456789abcdef",
                "0123456789abcdef",
                TraceFlags.getSampled(),
                TraceState.getDefault());
        try (var scope = Context.current().with(Span.wrap(spanContext)).makeCurrent()) {
            adapter.publishPaymentStateChangedEvent(payment);
        }

        verify(jdbcTemplate).update(contains("INSERT INTO PAYMENT_OUTBOX"),
                any(UUID.class), eq(paymentId), eq(1L), eq("PaymentStateChanged"),
                argThat((String json) -> json.contains("eventId")
                        && json.contains("aggregateVersion") && json.contains(paymentId.toString())),
                argThat((String json) -> json.contains("0123456789abcdef0123456789abcdef")
                        && json.contains("0123456789abcdef") && json.contains("traceparent")));
        verifyNoMoreInteractions(jdbcTemplate);
    }
}
