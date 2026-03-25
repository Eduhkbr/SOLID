package br.com.fiap.PaymentSolidApi.infrastructure.adapter;

import br.com.fiap.PaymentSolidApi.application.domain.model.Receipt;
import br.com.fiap.PaymentSolidApi.application.port.out.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Primary
public class ReceiptRepositoryWithFallbackAdapter implements ReceiptRepository {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptRepositoryWithFallbackAdapter.class);

    private final MongoReceiptAdapter mongoAdapter;
    private final PostgresReceiptAdapter postgresAdapter;

    public ReceiptRepositoryWithFallbackAdapter(
            @Qualifier("mongoReceiptAdapter") MongoReceiptAdapter mongoAdapter,
            @Qualifier("postgresReceiptAdapter") PostgresReceiptAdapter postgresAdapter) {
        this.mongoAdapter = mongoAdapter;
        this.postgresAdapter = postgresAdapter;
    }

    @Override
    public Receipt save(Receipt receipt) {
        try {
            logger.info("Tentando salvar comprovante no MongoDB...");
            return mongoAdapter.create(receipt);
        } catch (DataAccessResourceFailureException e) {
            logger.warn("Falha ao conectar com o MongoDB. Acionando fallback para PostgreSQL.", e);
            return postgresAdapter.create(receipt);
        }
    }

    @Override
    public Optional<Receipt> findByPaymentId(UUID paymentId) {
        try {
            logger.info("Tentando buscar comprovante no MongoDB...");
            Optional<Receipt> receipt = mongoAdapter.findByPaymentId(paymentId);
            if (receipt.isPresent()) {
                return receipt;
            }
            logger.info("Comprovante não encontrado no MongoDB, tentando no PostgreSQL...");
            return postgresAdapter.findByPaymentId(paymentId);
        } catch (DataAccessResourceFailureException e) {
            logger.warn("Falha ao conectar com o MongoDB. Acionando fallback para PostgreSQL.", e);
            return postgresAdapter.findByPaymentId(paymentId);
        }
    }
}
