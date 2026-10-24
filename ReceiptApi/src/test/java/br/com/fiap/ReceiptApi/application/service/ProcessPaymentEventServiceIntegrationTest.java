package br.com.fiap.ReceiptApi.application.service;

import br.com.fiap.ReceiptApi.application.port.in.ProcessPaymentEventUseCase;
import br.com.fiap.ReceiptApi.application.port.out.ReceiptRepository;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import br.com.fiap.ReceiptApi.domain.model.Receipt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.rabbitmq.username=test",
        "spring.rabbitmq.password=test",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class ProcessPaymentEventServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ProcessPaymentEventUseCase processPaymentEvent;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoBean
    ReceiptRepository receiptRepository;

    @Test
    void receiptFailureRollsBackDeduplicationRecordAndAllowsRedelivery() {
        UUID paymentId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PaymentVO payment = new PaymentVO(paymentId, "PIX", new BigDecimal("42.50"), "PENDING", now, now);
        when(receiptRepository.findByPaymentId(paymentId)).thenReturn(java.util.Optional.empty());
        doThrow(new IllegalStateException("falha simulada ao gravar recibo"))
                .when(receiptRepository).create(any(Receipt.class));

        assertThrows(IllegalStateException.class, () -> processPaymentEvent.process(eventId, 1L, payment));
        assertEquals(0, processedEventCount(eventId));

        // A mesma identidade pode ser aplicada novamente após corrigir a falha transitória.
        reset(receiptRepository);
        when(receiptRepository.findByPaymentId(paymentId)).thenReturn(java.util.Optional.empty());
        processPaymentEvent.process(eventId, 1L, payment);

        assertEquals(1, processedEventCount(eventId));
        verify(receiptRepository).create(any(Receipt.class));
    }

    private int processedEventCount(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PROCESSED_EVENTS WHERE EVENT_ID = ?", Integer.class, eventId);
    }
}
