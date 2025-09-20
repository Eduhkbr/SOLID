package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.jpa;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
}
