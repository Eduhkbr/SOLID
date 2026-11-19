package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.repository.jpa;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from PaymentJpaEntity p where p.id = :id")
    java.util.Optional<PaymentJpaEntity> findForUpdate(@org.springframework.data.repository.query.Param("id") UUID id);
}
