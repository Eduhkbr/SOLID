package br.com.fiap.ReceiptApi.infrastructure.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

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

    @Value("${app.rabbitmq.queue.receipt.ttl:60000}")
    private long receiptQueueTtl;

    @Bean
    public Queue receiptQueue() {
        // Configura a fila principal para encaminhar mensagens para a DLX em caso de rejeição/expiração
        return QueueBuilder.durable(receiptQueue)
                .withArgument("x-dead-letter-exchange", paymentDlxExchange)
                .withArgument("x-dead-letter-routing-key", receiptDlqRoutingKey)
                .withArgument("x-message-ttl", receiptQueueTtl)
                .build();
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

    /**
     * Retry interceptor: tenta reprocessar a mensagem N vezes e, se falhar, publica a mensagem no DLX
     */
    @Bean
    public RetryOperationsInterceptor rabbitRetryInterceptor(RabbitTemplate rabbitTemplate) {
        // republish to DLX when retries are exhausted
        RepublishMessageRecoverer recoverer = new RepublishMessageRecoverer(rabbitTemplate, paymentDlxExchange, receiptDlqRoutingKey);

        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .recoverer(recoverer)
                .build();
    }

    /**
     * Container factory que aplica o interceptor de retry (assim @RabbitListener usa retry automaticamente)
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor rabbitRetryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        // adiciona o interceptor na cadeia de advices
        factory.setAdviceChain(new Advice[]{rabbitRetryInterceptor});
        return factory;
    }

    /**
     * Configura o Spring para usar JSON na serialização/desserialização das mensagens.
     * Adiciona o JavaTimeModule para lidar com datas como LocalDateTime.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
