package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.*;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import br.com.fiap.PaymentSolidApi.application.port.in.PaymentService;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.PaymentMapper;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.entity.ReceiptJpaEntity;
import br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.repository.mongo.ReceiptRepository;
import br.com.fiap.paymentsolidiapi.api.PaymentsApi;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.ReceiptResponseDTO;
import org.openapitools.model.PaymentResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;

@RestController
@RequestMapping("/api")
public class PaymentController implements PaymentsApi {

    private final DataSource dataSource;
    private final ReceiptRepository receiptRepository;
    private final PaymentService paymentService;

    public PaymentController(DataSource dataSource, ReceiptRepository receiptRepository, PaymentService paymentService) {
        this.paymentService = paymentService;
        this.dataSource = dataSource;
        this.receiptRepository = receiptRepository;
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        final var createdPayment = paymentService.create(PaymentMapper.fromRequestDto(request));
        final var uri = URI.create("/api/payments/" + createdPayment.getId());

        return ResponseEntity.created(uri).body(PaymentMapper.toResponseDto(createdPayment));
    }
    
    @Override
    public ResponseEntity<PaymentResponseDTO> findPaymentById(@PathVariable("id") UUID id) {
        var paymentOpt = paymentService.findById(id);
        return paymentOpt.map(payment ->
                ResponseEntity.ok(PaymentMapper.toResponseDto(payment))).orElseGet(() ->
                ResponseEntity.noContent().build());
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