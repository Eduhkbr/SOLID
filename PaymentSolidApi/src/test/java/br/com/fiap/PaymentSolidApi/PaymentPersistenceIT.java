package br.com.fiap.PaymentSolidApi;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.application.domain.exception.PaymentRefundException;
import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging.PaymentOutboxRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PaymentPersistenceIT {
    @Autowired PaymentService payments;
    @Autowired PaymentOutboxRepository outbox;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @BeforeEach void clean() { jdbc.execute("TRUNCATE PAYMENT_OUTBOX, PAYMENTS"); }
    private Payment create() { return payments.create(Payment.create(Payment.PaymentMethod.BOLETO,new BigDecimal("42.50"),null,null,null)); }
    private int count(String table) { return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Integer.class); }

    @Test void commitsPaymentAndEventWithSameExactAmount() {
        Payment payment=create();
        assertThat(count("PAYMENTS")).isEqualTo(1);
        assertThat(count("PAYMENT_OUTBOX")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT (PAYLOAD->>'amount')::numeric FROM PAYMENT_OUTBOX",BigDecimal.class)).isEqualByComparingTo("42.50");
        assertThat(jdbc.queryForObject("SELECT AGGREGATE_ID FROM PAYMENT_OUTBOX",UUID.class)).isEqualTo(payment.getId());
    }
    @Test void outboxFailureRollsBackPayment() {
        jdbc.execute("ALTER TABLE PAYMENT_OUTBOX ADD CONSTRAINT simulated_failure CHECK(false) NOT VALID");
        try { assertThatThrownBy(this::create).isInstanceOf(RuntimeException.class); }
        finally { jdbc.execute("ALTER TABLE PAYMENT_OUTBOX DROP CONSTRAINT simulated_failure"); }
        assertThat(count("PAYMENTS")).isZero();
        assertThat(count("PAYMENT_OUTBOX")).isZero();
    }
    @Test void outerRollbackRemovesPaymentAndEvent() {
        assertThatThrownBy(() -> new TransactionTemplate(transactions).execute(status -> {
            create(); throw new IllegalStateException("falha após registrar intenção");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(count("PAYMENTS")).isZero();
        assertThat(count("PAYMENT_OUTBOX")).isZero();
    }
    @Test void concurrentRefundsProduceOneTransitionAndOneRefundEvent() throws Exception {
        Payment payment=create();
        try(var pool=Executors.newFixedThreadPool(2)) {
            CountDownLatch start=new CountDownLatch(1);
            Callable<Boolean> refund=() -> { start.await(); try { payments.refundPayment(payment.getId()); return true; }
                catch(PaymentRefundException expected) { return false; } };
            Future<Boolean> a=pool.submit(refund),b=pool.submit(refund); start.countDown();
            assertThat(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
        }
        assertThat(jdbc.queryForObject("SELECT STATUS FROM PAYMENTS",String.class)).isEqualTo("REFUNDED");
        assertThat(count("PAYMENT_OUTBOX")).isEqualTo(2);
    }
    @Test void concurrentRelaysClaimDifferentEvents() throws Exception {
        create(); create();
        try(var pool=Executors.newFixedThreadPool(2)) {
            var a=pool.submit(() -> outbox.claimBatch(1));
            var b=pool.submit(() -> outbox.claimBatch(1));
            var first=a.get(10,TimeUnit.SECONDS); var second=b.get(10,TimeUnit.SECONDS);
            assertThat(first).hasSize(1); assertThat(second).hasSize(1);
            assertThat(first.getFirst().eventId()).isNotEqualTo(second.getFirst().eventId());
            assertThat(outbox.claimBatch(1)).isEmpty();
        }
    }

    @Test void expiredLeaseCanBeReclaimedButOldAttemptCannotOverwriteNewOwner() {
        create();
        var first=outbox.claimBatch(1).getFirst();
        jdbc.update("UPDATE PAYMENT_OUTBOX SET LOCKED_UNTIL=CURRENT_TIMESTAMP-INTERVAL '1 second'");
        var second=outbox.claimBatch(1).getFirst();
        assertThat(second.attemptCount()).isEqualTo(first.attemptCount()+1);
        outbox.markPublished(first.eventId(), first.attemptCount());
        outbox.scheduleRetry(first.eventId(), first.attemptCount(), java.time.Duration.ZERO, "old attempt");
        assertThat(jdbc.queryForObject("SELECT LOCKED_UNTIL IS NOT NULL AND PUBLISHED_AT IS NULL FROM PAYMENT_OUTBOX",Boolean.class)).isTrue();
        outbox.markPublished(second.eventId(), second.attemptCount());
        assertThat(outbox.pendingCount()).isZero();
    }
}
