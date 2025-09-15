package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.jpa;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PostgresReceiptJpaRepository extends JpaRepository<ReceiptJpaEntity, UUID> {
}

