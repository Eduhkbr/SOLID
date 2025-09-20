package br.com.fiap.ReceiptApi.infrastructure.adapter.out.repository;

import br.com.fiap.ReceiptApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReceiptJpaRepository extends JpaRepository<ReceiptJpaEntity, UUID> {
}

