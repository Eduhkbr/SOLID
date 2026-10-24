package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import java.util.UUID;

public record OutboxMessage(UUID eventId, String payload, String traceContext, int attemptCount) { }
