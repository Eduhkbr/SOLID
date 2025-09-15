package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import java.math.BigDecimal;
import java.sql.*;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.paymentsolidiapi.api.PaymentsApi;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.ReceiptResponseDTO;
import org.openapitools.model.PaymentResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.ReceiptRepository;

@RestController
@RequestMapping("/api")
public class PaymentController implements PaymentsApi {

    private final DataSource dataSource;
    private final ReceiptRepository receiptRepository;

    public PaymentController(DataSource dataSource, ReceiptRepository receiptRepository) {
        this.dataSource = dataSource;
        this.receiptRepository = receiptRepository;
    }

    @Override
    public ResponseEntity<String> createPayment(@RequestBody PaymentRequestDTO request) {
        try {
            // Extração de dados do request
            String tipo = request.getPaymentMethod().getValue();
            BigDecimal valor = request.getAmount();
            String chavePix = null;

            // VIOLAÇÃO DE TODOS OS PRINCÍPIPIOS SOLID

            // 1. Validação misturada (Violação Single Responsibility Principle)
            // SRP: Controller fazendo validação (deveria ser responsabilidade de um Validator)
            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Valor do pagamento é inválido.");
            }
            if (tipo == null || tipo.isBlank()) {
                return ResponseEntity.badRequest().body("Tipo de pagamento é obrigatório.");
            }
            chavePix = request.getPixKey();
            if ((tipo.equals("CREDIT_CARD") || tipo.equals("BOLETO")) && (chavePix != null && !chavePix.isBlank())) {
                return ResponseEntity.badRequest().body("Chave PIX não deve ser incluída para pagamento por " + tipo + ".");
            }

            // 2. Lógica de negócio no controller (Violação Single Responsibility Principle e Open/Closed Principle)
            if (tipo.equals("CREDIT_CARD")) {
                String numeroCartao = request.getCardNumber();
                String cvv = request.getCvv();

                // Validação de cartão inline
                if (numeroCartao == null || numeroCartao.length() != 16) {
                    return ResponseEntity.badRequest().body("Número do cartão inválido.");
                }
                if (cvv == null || cvv.length() != 3) {
                    return ResponseEntity.badRequest().body("CVV do cartão inválido.");
                }

                System.out.println("Validando limite do cartão...");
                System.out.println("Processando pagamento com Cartão de Crédito no valor de " + valor);
            } else if (tipo.equals("PIX")) {
                if (chavePix == null || chavePix.isBlank()) {
                    return ResponseEntity.badRequest().body("Chave PIX é obrigatória.");
                }
                System.out.println("Processando pagamento com PIX no valor de " + valor);
                System.out.println("Gerando QR Code para a chave: " + chavePix);

            } else if (tipo.equals("BOLETO")) {
                System.out.println("Processando pagamento com Boleto no valor de " + valor);
                System.out.println("Gerando linha digitável e enviando para o e-mail do cliente.");
            } else {
                return ResponseEntity.badRequest().body("Tipo de pagamento não suportado.");
            }

            // 4. Persistência de dados direto no controller (Violação Single Responsibility Principle)
            UUID pagamentoId = UUID.randomUUID();
            try (Connection conn = dataSource.getConnection()) {
                // Salvar informações do pagamento
                String sqlPagamento = "INSERT INTO PAYMENTS (ID, PAYMENT_METHOD, AMOUNT, STATUS, EMAIL, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmtPagamento = conn.prepareStatement(sqlPagamento);
                stmtPagamento.setObject(1, pagamentoId);
                stmtPagamento.setString(2, tipo);
                stmtPagamento.setBigDecimal(3, valor);
                stmtPagamento.setString(4, PaymentStatus.APPROVED.name());
                stmtPagamento.setString(5, chavePix);
                stmtPagamento.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                stmtPagamento.executeUpdate();

                // Tentar salvar comprovante no MongoDB
                String receiptData = generateReceiptData(tipo, valor, pagamentoId.toString(), PaymentStatus.APPROVED);
                try {
                    ReceiptJpaEntity r = ReceiptJpaEntity.builder()
                            .paymentId(pagamentoId)
                            .receiptData(receiptData)
                            .createdAt(LocalDateTime.now())
                            .build();
                    receiptRepository.save(r);
                    System.out.println("Comprovante salvo no MongoDB para pagamento: " + pagamentoId);
                } catch (Exception mongoEx) {
                    // fallback: salvar em tabela SQL se Mongo não estiver disponível
                    String sqlComprovante = "INSERT INTO PAYMENT_RECEIPTS (PAYMENT_ID, RECEIPT_DATA, CREATED_AT) VALUES (?, ?, ?)";
                    PreparedStatement stmtComprovante = conn.prepareStatement(sqlComprovante);
                    stmtComprovante.setObject(1, pagamentoId);
                    stmtComprovante.setString(2, receiptData);
                    stmtComprovante.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                    stmtComprovante.executeUpdate();

                    System.out.println("Comprovante salvo no banco relacional (fallback) para pagamento: " + pagamentoId);
                }
                conn.close();

                System.out.println("Pagamento salvo no banco de dados com ID: " + pagamentoId);
                System.out.println("Comprovante gerado e salvo.");
            }

            return ResponseEntity.ok("Pagamento processado com sucesso! ID: " + pagamentoId);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno no processamento do pagamento.");
        }
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> findReceiptById(@PathVariable("id") UUID id) {
        try {
            // Busca no MongoDB
            Optional<ReceiptJpaEntity> receiptOptional = receiptRepository.findById(id);
            if (receiptOptional.isPresent()) {
                ReceiptJpaEntity receiptJpaEntity = receiptOptional.get();
                ReceiptResponseDTO responseDTO = new ReceiptResponseDTO();
                responseDTO.setReceiptData(receiptJpaEntity.getReceiptData());
                responseDTO.setPaymentId(receiptJpaEntity.getPaymentId());
                LocalDateTime createdAt = receiptJpaEntity.getCreatedAt();
                ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
                responseDTO.setCreatedAt(createdAt.atZone(zoneId).toOffsetDateTime());
                return ResponseEntity.ok(responseDTO);
            }

            // Busca no Postgres
            try (Connection conn = dataSource.getConnection()) {
                String sql = "SELECT * FROM PAYMENT_RECEIPTS WHERE PAYMENT_ID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setObject(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    ReceiptResponseDTO responseDTO = new ReceiptResponseDTO();
                    responseDTO.setPaymentId(rs.getObject("PAYMENT_ID", UUID.class));
                    responseDTO.setReceiptData(rs.getString("RECEIPT_DATA"));

                    Timestamp createdAtTimestamp = rs.getTimestamp("CREATED_AT");
                    LocalDateTime createdAt = createdAtTimestamp.toLocalDateTime();
                    ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
                    responseDTO.setCreatedAt(createdAt.atZone(zoneId).toOffsetDateTime());

                    return ResponseEntity.ok(responseDTO);
                }
            }
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> findPaymentById(@PathVariable("id") UUID id) {
        try {
            try (Connection conn = dataSource.getConnection()) {
                String sql = "SELECT ID, PAYMENT_METHOD, AMOUNT, STATUS, CREATED_AT FROM PAYMENTS WHERE ID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setObject(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    PaymentResponseDTO responseDTO = new PaymentResponseDTO();
                    responseDTO.setPaymentId(rs.getObject("ID", UUID.class));
                    responseDTO.setPaymentMethod(PaymentResponseDTO.PaymentMethodEnum.valueOf(rs.getString("PAYMENT_METHOD")));
                    responseDTO.setAmount(rs.getBigDecimal("AMOUNT"));
                    responseDTO.setStatus(PaymentResponseDTO.StatusEnum.valueOf(rs.getString("STATUS")));

                    Timestamp createdAtTimestamp = rs.getTimestamp("CREATED_AT");
                    LocalDateTime createdAt = createdAtTimestamp.toLocalDateTime();
                    ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
                    responseDTO.setProcessedAt(createdAt.atZone(zoneId).toOffsetDateTime());

                    return ResponseEntity.ok(responseDTO);
                }
            }
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<String> refundPayment(@PathVariable("id") UUID id) {
        try {
            try (Connection conn = dataSource.getConnection()) {
                String sql = "SELECT PAYMENT_METHOD, STATUS FROM PAYMENTS WHERE ID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setObject(1, id);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    return ResponseEntity.noContent().build();
                }

                String metodo = rs.getString("PAYMENT_METHOD");

                // VIOLAÇÃO LSP/ISP: comportamento diferente para cada tipo
                return switch (metodo) {
                    case "PIX" -> ResponseEntity.badRequest().body("PIX não permite estorno.");
                    case "BOLETO" -> {
                        atualizarStatusParaRefunded(conn, id);
                        yield ResponseEntity.ok("Boleto cancelado com sucesso.");
                    }
                    case "CREDIT_CARD" -> {
                        atualizarStatusParaRefunded(conn, id);
                        yield ResponseEntity.ok("Estorno no cartão realizado com sucesso.");
                    }
                    default -> ResponseEntity.badRequest().body("Tipo de pagamento não suportado.");
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao processar estorno.");
        }
    }

    // Atualiza o status do pagamento para REFUNDED no banco.
    private void atualizarStatusParaRefunded(Connection conn, UUID pagamentoId) throws SQLException {
        String updateSql = "UPDATE PAYMENTS SET STATUS = ?, UPDATED_AT = ? WHERE ID = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setString(1, PaymentStatus.REFUNDED.name());
            updateStmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));;
            updateStmt.setObject(3, pagamentoId);
            updateStmt.executeUpdate();
        }
    }

    // Metodo para gerar dados do comprovante
    private String generateReceiptData(String tipo, BigDecimal valor, String pagamentoId, PaymentStatus status) {
        // Exemplo simples de geração de comprovante
        return String.format(
                "COMPROVANTE DE PAGAMENTO\n" +
                        "ID: %s\n" +
                        "Tipo: %s\n" +
                        "Valor: R$ %.2f\n" +
                        "Data: %s\n" +
                        "Status: %s",
                pagamentoId,
                tipo,
                valor,
                LocalDateTime.now(),
                status
        );
    }
}