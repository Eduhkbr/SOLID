package br.com.fiap.PaymentSolidApi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaRepositories(basePackages = "br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.jpa")
@EnableMongoRepositories(basePackages = "br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.mongo")
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
