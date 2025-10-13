package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.jpa.PaymentJpaRepository;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.mappers.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentRepositoryAdapterTest {
    private PaymentJpaRepository jpaRepository;
    private PaymentRepositoryAdapter adapter;

    @BeforeEach
    void setup() {
        jpaRepository = Mockito.mock(PaymentJpaRepository.class);
        adapter = new PaymentRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_persists_and_returns_payment() {
        Payment payment = Payment.create(Payment.PaymentMethod.PIX, new BigDecimal("10.00"), "pix-key", null, null);
        PaymentJpaEntity entity = new PaymentJpaEntity();
        try (MockedStatic<PaymentMapper> mapperMock = Mockito.mockStatic(PaymentMapper.class)) {
            mapperMock.when(() -> PaymentMapper.toEntity(any())).thenReturn(entity);
            mapperMock.when(() -> PaymentMapper.toDomain(entity)).thenReturn(payment);
            Mockito.when(jpaRepository.save(entity)).thenReturn(entity);
            Payment result = adapter.save(payment);
            assertThat(result).isEqualTo(payment);
        }
    }

    @Test
    void findById_returns_payment_when_found() {
        UUID id = UUID.randomUUID();
        PaymentJpaEntity entity = new PaymentJpaEntity();
        Payment payment = Payment.create(Payment.PaymentMethod.BOLETO, new BigDecimal("20.00"), null, null, null);
        Mockito.when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        try (MockedStatic<PaymentMapper> mapperMock = Mockito.mockStatic(PaymentMapper.class)) {
            mapperMock.when(() -> PaymentMapper.toDomain(entity)).thenReturn(payment);
            Optional<Payment> result = adapter.findById(id);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(payment);
        }
    }

    @Test
    void findById_returns_empty_when_not_found() {
        UUID id = UUID.randomUUID();
        Mockito.when(jpaRepository.findById(id)).thenReturn(Optional.empty());
        Optional<Payment> result = adapter.findById(id);
        assertThat(result).isEmpty();
    }
}
