package br.com.fiap.PaymentSolidApi.infrastructure.config;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
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
     * Jackson 3 inclui suporte a java.time; o contrato do evento permanece JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper objectMapper = JsonMapper.builder().build();
        return new JacksonJsonMessageConverter(objectMapper);
    }

}
