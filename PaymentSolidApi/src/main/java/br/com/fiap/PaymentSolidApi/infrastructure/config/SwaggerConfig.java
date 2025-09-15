package br.com.fiap.PaymentSolidApi.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Payment SOLID API", 
        version = "1.0.0",
        description = "API de Processamento de Pagamentos"
    ),
    servers = {
        @Server(url = "http://localhost:8082", description = "Servidor de Desenvolvimento")
    }
)
public class SwaggerConfig {
}