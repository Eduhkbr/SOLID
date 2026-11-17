package br.com.fiap.ReceiptApi.application.service;

import br.com.fiap.ReceiptApi.application.port.in.ProcessPaymentEventUseCase;
import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ReceiptPersistenceIT {
    @Autowired ProcessPaymentEventUseCase process;
    @Autowired JdbcTemplate jdbc;
    @BeforeEach void clean() { jdbc.execute("TRUNCATE PROCESSED_EVENTS, RECEIPTS"); }
    private PaymentVO payment(UUID id,String status) { var now=LocalDateTime.of(2026,1,1,12,0); return new PaymentVO(id,"BOLETO",new BigDecimal("42.50"),status,now,now); }
    private int count(String table) { return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Integer.class); }
    @Test void duplicateDoesNotRewriteReceipt() {
        UUID id=UUID.randomUUID(),event=UUID.randomUUID();
        process.process(event,1,payment(id,"PENDING"));
        String before=jdbc.queryForObject("SELECT RECEIPT_DATA FROM RECEIPTS",String.class);
        process.process(event,1,payment(id,"PENDING"));
        assertThat(count("RECEIPTS")).isEqualTo(1); assertThat(count("PROCESSED_EVENTS")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT RECEIPT_DATA FROM RECEIPTS",String.class)).isEqualTo(before);
    }
    @Test void oldCreationAfterRefundCannotRegressFinalState() {
        UUID id=UUID.randomUUID();
        process.process(UUID.randomUUID(),2,payment(id,"REFUNDED"));
        process.process(UUID.randomUUID(),1,payment(id,"PENDING"));
        assertThat(jdbc.queryForObject("SELECT RECEIPT_DATA FROM RECEIPTS",String.class)).contains("ESTORNO REALIZADO");
        assertThat(count("RECEIPTS")).isEqualTo(1); assertThat(count("PROCESSED_EVENTS")).isEqualTo(2);
    }
    @Test void failedReceiptWriteRollsBackDeduplicationAndAllowsRedelivery() {
        UUID id=UUID.randomUUID(),event=UUID.randomUUID();
        jdbc.execute("ALTER TABLE RECEIPTS ADD CONSTRAINT simulated_failure CHECK(false) NOT VALID");
        try { assertThatThrownBy(() -> process.process(event,1,payment(id,"PENDING"))).isInstanceOf(RuntimeException.class); }
        finally { jdbc.execute("ALTER TABLE RECEIPTS DROP CONSTRAINT simulated_failure"); }
        assertThat(count("PROCESSED_EVENTS")).isZero(); assertThat(count("RECEIPTS")).isZero();
        process.process(event,1,payment(id,"PENDING"));
        assertThat(count("PROCESSED_EVENTS")).isEqualTo(1); assertThat(count("RECEIPTS")).isEqualTo(1);
    }
    @Test void concurrentCreationAndRefundKeepOneFinalReceipt() throws Exception {
        UUID id=UUID.randomUUID();
        try(var pool=Executors.newFixedThreadPool(2)) {
            CountDownLatch start=new CountDownLatch(1);
            Future<?> a=pool.submit(() -> { try { start.await(); process.process(UUID.randomUUID(),1,payment(id,"PENDING")); } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); } });
            Future<?> b=pool.submit(() -> { try { start.await(); process.process(UUID.randomUUID(),2,payment(id,"REFUNDED")); } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); } });
            start.countDown(); a.get(20,TimeUnit.SECONDS); b.get(20,TimeUnit.SECONDS);
        }
        assertThat(count("RECEIPTS")).isEqualTo(1); assertThat(count("PROCESSED_EVENTS")).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT RECEIPT_DATA FROM RECEIPTS",String.class)).contains("ESTORNO REALIZADO");
    }
}
