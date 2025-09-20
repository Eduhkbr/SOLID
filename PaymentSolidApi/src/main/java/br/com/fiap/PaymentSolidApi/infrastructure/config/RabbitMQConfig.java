package br.com.fiap.PaymentSolidApi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.payment}")
    private String paymentExchange;

    /**
     * Define a exchange principal para eventos de pagamento.
     * Os produtores enviam mensagens para esta exchange.
     */
    @Bean
    public Exchange paymentExchange() {
        return ExchangeBuilder.topicExchange(paymentExchange).durable(true).build();
    }

    /**
     * Configura o Spring para usar JSON na serialização/desserialização das mensagens.
     * Adiciona o JavaTimeModule para lidar corretamente com datas como LocalDateTime.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

}
