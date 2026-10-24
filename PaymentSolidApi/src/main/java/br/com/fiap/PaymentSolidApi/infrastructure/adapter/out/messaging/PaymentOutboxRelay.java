package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessageDeliveryMode;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "app.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentOutboxRelay {

    private static final Logger logger = LoggerFactory.getLogger(PaymentOutboxRelay.class);
    private final PaymentOutboxRepository outbox;
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;
    private final long confirmTimeoutMillis;
    private final io.micrometer.core.instrument.Counter publishFailures;

    public PaymentOutboxRelay(PaymentOutboxRepository outbox, RabbitTemplate rabbitTemplate,
                              MeterRegistry meterRegistry,
                              @Value("${app.rabbitmq.exchange.payment}") String exchange,
                              @Value("${app.rabbitmq.routingkey.receipt}") String routingKey,
                              @Value("${app.outbox.confirm-timeout:5000}") long confirmTimeoutMillis) {
        this.outbox = outbox;
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.confirmTimeoutMillis = confirmTimeoutMillis;
        this.publishFailures = meterRegistry.counter("payment.outbox.publish.failures");
        this.rabbitTemplate.setReturnsCallback(returned -> logger.warn(
                "RabbitMQ devolveu publicação sem rota: exchange={}, routingKey={}, reply={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));
        meterRegistry.gauge("payment.outbox.pending", outbox, PaymentOutboxRepository::pendingCount);
        meterRegistry.gauge("payment.outbox.oldest.pending.seconds", outbox,
                PaymentOutboxRepository::oldestPendingAgeSeconds);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void drainAfterStartup() {
        publishBatch();
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval:1000}")
    public void publishBatch() {
        for (OutboxMessage message : outbox.claimBatch(10)) {
            try {
                publish(message);
                outbox.markPublished(message.eventId());
            } catch (Exception exception) {
                publishFailures.increment();
                Duration delay = retryDelay(message.attemptCount());
                outbox.scheduleRetry(message.eventId(), delay, exception.getMessage());
                logger.warn("Falha ao publicar evento {}. Nova tentativa em {} ms.",
                        message.eventId(), delay.toMillis());
            }
        }
    }

    private void publish(OutboxMessage event) throws Exception {
        CorrelationData correlation = new CorrelationData(event.eventId().toString());
        Message message = MessageBuilder.withBody(event.payload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(event.eventId().toString())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        rabbitTemplate.send(exchange, routingKey, message, correlation);

        CorrelationData.Confirm confirm = correlation.getFuture()
                .get(confirmTimeoutMillis, TimeUnit.MILLISECONDS);
        if (!confirm.isAck()) {
            throw new IllegalStateException("RabbitMQ rejeitou a publicação: " + confirm.getReason());
        }
        if (correlation.getReturned() != null) {
            throw new IllegalStateException("RabbitMQ não encontrou uma fila para a mensagem publicada");
        }
    }

    private Duration retryDelay(int attempt) {
        long exponential = Math.min(300_000L, 1_000L << Math.min(attempt, 8));
        long jittered = (long) (exponential * ThreadLocalRandom.current().nextDouble(0.75, 1.25));
        return Duration.ofMillis(jittered);
    }
}
