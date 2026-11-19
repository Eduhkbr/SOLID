package br.com.fiap.ReceiptApi.infrastructure.adapter.config;

import tools.jackson.databind.json.JsonMapper;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecovererWithConfirms;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.payment}")
    private String paymentExchange;

    @Value("${app.rabbitmq.queue.receipt}")
    private String receiptQueue;

    @Value("${app.rabbitmq.routingkey.receipt}")
    private String receiptRoutingKey;

    @Value("${app.rabbitmq.exchange.payment.dlx}")
    private String paymentDlxExchange;

    @Value("${app.rabbitmq.queue.receipt.dlq}")
    private String receiptDlq;

    @Value("${app.rabbitmq.routingkey.receipt.dlq}")
    private String receiptDlqRoutingKey;

    @Value("${app.rabbitmq.queue.receipt.ttl:0}")
    private long receiptQueueTtl;

    @Bean
    public Queue receiptQueue() {
        // Configura a fila principal para encaminhar mensagens para a DLX em caso de rejeição/expiração
        QueueBuilder builder = QueueBuilder.durable(receiptQueue)
                .withArgument("x-dead-letter-exchange", paymentDlxExchange)
                .withArgument("x-dead-letter-routing-key", receiptDlqRoutingKey);
        if (receiptQueueTtl > 0) builder.withArgument("x-message-ttl", receiptQueueTtl);
        return builder.build();
    }

    @Bean
    public Exchange paymentExchange() {
        return ExchangeBuilder.topicExchange(paymentExchange).durable(true).build();
    }

    @Bean
    public Binding receiptBinding(Queue receiptQueue, Exchange paymentExchange) {
        return BindingBuilder
                .bind(receiptQueue)
                .to(paymentExchange)
                .with(receiptRoutingKey)
                .noargs();
    }

    /**
     * Dead-letter exchange (DLX) e dead-letter queue (DLQ) beans
     */
    @Bean
    public Exchange paymentDlxExchange() {
        return ExchangeBuilder.topicExchange(paymentDlxExchange).durable(true).build();
    }

    @Bean
    public Queue receiptDlq() {
        return QueueBuilder.durable(receiptDlq).build();
    }

    @Bean
    public Binding receiptDlqBinding(Queue receiptDlq, Exchange paymentDlxExchange) {
        return BindingBuilder
                .bind(receiptDlq)
                .to(paymentDlxExchange)
                .with(receiptDlqRoutingKey)
                .noargs();
    }

    /** O configurer do Boot aplica retry limitado e só confirma a entrega após o recoverer. */
    @Bean
    public org.springframework.amqp.rabbit.retry.MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecovererWithConfirms(rabbitTemplate, paymentDlxExchange, receiptDlqRoutingKey,
                CachingConnectionFactory.ConfirmType.CORRELATED);
    }

    /**
     * Configura o Spring para usar JSON na serialização/desserialização das mensagens.
     * Jackson 3 inclui suporte a java.time; o contrato do evento permanece JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper objectMapper = JsonMapper.builder().build();
        return new JacksonJsonMessageConverter(objectMapper);
    }
}
