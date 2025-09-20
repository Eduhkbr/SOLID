package br.com.fiap.ReceiptApi.infrastructure.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    public Queue receiptQueue() {
        return QueueBuilder.durable(receiptQueue).build();
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
