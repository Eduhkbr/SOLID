package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import br.com.fiap.PaymentSolidApi.application.domain.PaymentStatus;
import br.com.fiap.paymentsolidiapi.api.PaymentsApi;
import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.PaymentResponseDTO;
import org.openapitools.model.ReceiptResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ️ VERSÃO COM VIOLAÇÕES PROPOSITAIS DE TODOS OS 5 PRINCÍPIOS SOLID
 */
@RestController
@RequestMapping("/api/v1")
public class PaymentController implements PaymentsApi, PaymentReportOperations {

    // =========================================================================
    // VIOLAÇÃO [DIP] - Dependency Inversion Principle
    // Dependência direta de detalhes de infraestrutura (JDBC, credenciais)
    // em vez de depender de abstrações (interfaces Repository).
    // =========================================================================
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/paymentdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    // =========================================================================
    // VIOLAÇÃO [SRP] - Single Responsibility Principle
    // O Controller acumula responsabilidades de: logging, envio de email,
    // validação, acesso a dados (JDBC), geração de comprovantes e resposta HTTP.
    // =========================================================================
    private void logToFile(String message) {
        System.out.println("[LOG " + LocalDateTime.now() + "] " + message);
    }

    private void sendEmailNotification(String toEmail, String subject, String body) {
        System.out.println("Enviando email para: " + toEmail);
        System.out.println("Assunto: " + subject);
        System.out.println("Corpo: " + body);
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {

        // [SRP] Validação de negócio diretamente no controller
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor do pagamento deve ser positivo.");
        }
        if (request.getPaymentMethod() == null) {
            throw new RuntimeException("Método de pagamento é obrigatório.");
        }

        // =====================================================================
        // VIOLAÇÃO [OCP] - Open/Closed Principle
        // Cadeia de if/else que PRECISA SER MODIFICADA toda vez que surgir
        // um novo método de pagamento (ex: WALLET, CRYPTO).
        // Para adicionar um novo método, é necessário ABRIR e ALTERAR esta classe.
        // =====================================================================
        String paymentMethod = request.getPaymentMethod().getValue();
        if ("PIX".equals(paymentMethod)) {
            if (request.getPixKey() == null || request.getPixKey().isBlank()) {
                throw new RuntimeException("Chave PIX é obrigatória para pagamentos PIX.");
            }
            logToFile("Processando pagamento PIX com chave: " + request.getPixKey());
        } else if ("CREDIT_CARD".equals(paymentMethod)) {
            if (request.getCardNumber() == null || request.getCvv() == null) {
                throw new RuntimeException("Número do cartão e CVV são obrigatórios.");
            }
            logToFile("Processando pagamento com cartão: " + request.getCardNumber());
        } else if ("BOLETO".equals(paymentMethod)) {
            logToFile("Processando pagamento via boleto.");
        } else {
            // Para adicionar "WALLET" aqui, é preciso abrir este arquivo e modificá-lo
            throw new RuntimeException("Método de pagamento não suportado: " + paymentMethod);
        }

        UUID paymentId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // [DIP] Acesso direto ao banco via JDBC (depende de implementação concreta)
        // [SRP] Lógica de persistência e SQL diretamente no controller
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String sql = "INSERT INTO PAYMENTS (id, payment_method, amount, status, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, paymentId);
                stmt.setString(2, paymentMethod);
                stmt.setBigDecimal(3, request.getAmount());
                stmt.setString(4, PaymentStatus.PENDING.name());
                stmt.setTimestamp(5, Timestamp.valueOf(now));
                stmt.setTimestamp(6, Timestamp.valueOf(now));
                stmt.executeUpdate();
            }

            // [SRP] Geração de comprovante diretamente no controller
            String receiptData = String.format(
                    "COMPROVANTE DE PAGAMENTO\nID: %s\nTipo: %s\nValor: R$ %.2f\nData: %s\nStatus: %s",
                    paymentId, paymentMethod, request.getAmount(), now, PaymentStatus.PENDING
            );

            String receiptSql = "INSERT INTO PAYMENT_RECEIPTS (payment_id, receipt_data, created_at) " +
                                "VALUES (?, ?, ?)";
            try (PreparedStatement receiptStmt = conn.prepareStatement(receiptSql)) {
                receiptStmt.setObject(1, paymentId);
                receiptStmt.setString(2, receiptData);
                receiptStmt.setTimestamp(3, Timestamp.valueOf(now));
                receiptStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar pagamento: " + e.getMessage(), e);
        }

        // [SRP] Notificação por email no controller
        sendEmailNotification("cliente@example.com", "Pagamento Registrado",
                "Seu pagamento de ID " + paymentId + " foi registrado com sucesso.");

        // [SRP] Mapeamento manual do DTO no controller
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(paymentId);
        response.setPaymentMethod(PaymentResponseDTO.PaymentMethodEnum.valueOf(paymentMethod));
        response.setAmount(request.getAmount());
        response.setStatus(PaymentResponseDTO.StatusEnum.PENDING);
        response.setProcessedAt(now.atZone(ZoneId.systemDefault()).toOffsetDateTime());

        URI uri = URI.create("/api/payments/" + paymentId);
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> findPaymentById(@PathVariable("id") UUID id) {
        logToFile("Buscando pagamento com ID: " + id);

        // [DIP] Acesso direto ao banco via JDBC
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM PAYMENTS WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    PaymentResponseDTO dto = new PaymentResponseDTO();
                    dto.setPaymentId(rs.getObject("id", UUID.class));
                    dto.setPaymentMethod(
                            PaymentResponseDTO.PaymentMethodEnum.valueOf(rs.getString("payment_method")));
                    dto.setAmount(rs.getBigDecimal("amount"));
                    dto.setStatus(PaymentResponseDTO.StatusEnum.valueOf(rs.getString("status")));
                    dto.setProcessedAt(rs.getTimestamp("created_at").toLocalDateTime()
                            .atZone(ZoneId.systemDefault()).toOffsetDateTime());
                    return ResponseEntity.ok(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pagamento: " + e.getMessage(), e);
        }

        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // VIOLAÇÃO [LSP] - Liskov Substitution Principle
    // O contrato da interface PaymentsApi garante que este método retorna um
    // comprovante. Porém, esta implementação lança UnsupportedOperationException,
    // quebrando o contrato e surpreendendo qualquer código que dependa da
    // interface. Um objeto do tipo PaymentsApi não pode ser substituído por
    // este PaymentController sem quebrar o comportamento esperado.
    // =========================================================================
    @Override
    public ResponseEntity<ReceiptResponseDTO> findReceiptById(@PathVariable("id") UUID id) {
        throw new UnsupportedOperationException(
                "Funcionalidade de comprovante não implementada. Use /api/payments/{id} em vez disso."
        );
    }

    @Override
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable("id") UUID id) {
        logToFile("Processando estorno para pagamento: " + id);

        // [DIP] Acesso direto ao banco via JDBC
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String selectSql = "SELECT * FROM PAYMENTS WHERE id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setObject(1, id);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) {
                    throw new RuntimeException("Pagamento não encontrado com ID: " + id);
                }

                String method = rs.getString("payment_method");
                String status = rs.getString("status");

                // [OCP] Regras de negócio com if/else no controller
                if ("PIX".equals(method)) {
                    throw new RuntimeException(
                            "Estorno não permitido: o método 'PIX' não suporta esta operação.");
                }
                if (!"PENDING".equals(status)) {
                    throw new RuntimeException(
                            "Estorno não permitido: status deve ser 'PENDING', mas está '" + status + "'.");
                }

                LocalDateTime now = LocalDateTime.now();

                // [SRP] SQL de update direto no controller
                String updateSql = "UPDATE PAYMENTS SET status = ?, updated_at = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, PaymentStatus.REFUNDED.name());
                    updateStmt.setTimestamp(2, Timestamp.valueOf(now));
                    updateStmt.setObject(3, id);
                    updateStmt.executeUpdate();
                }

                // [SRP] Atualização do comprovante de estorno no controller
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String refundInfo = String.format(
                        "\n\n--- ESTORNO REALIZADO ---\nStatus: %s\nData do Estorno: %s",
                        PaymentStatus.REFUNDED, now.format(formatter)
                );

                String updateReceiptSql = "UPDATE PAYMENT_RECEIPTS SET receipt_data = receipt_data || ? " +
                                          "WHERE payment_id = ?";
                try (PreparedStatement receiptStmt = conn.prepareStatement(updateReceiptSql)) {
                    receiptStmt.setString(1, refundInfo);
                    receiptStmt.setObject(2, id);
                    receiptStmt.executeUpdate();
                }

                // [SRP] Notificação no controller
                sendEmailNotification("cliente@example.com", "Estorno Realizado",
                        "O pagamento de ID " + id + " foi estornado com sucesso.");

                PaymentResponseDTO response = new PaymentResponseDTO();
                response.setPaymentId(id);
                response.setPaymentMethod(PaymentResponseDTO.PaymentMethodEnum.valueOf(method));
                response.setAmount(rs.getBigDecimal("amount"));
                response.setStatus(PaymentResponseDTO.StatusEnum.REFUNDED);
                response.setProcessedAt(now.atZone(ZoneId.systemDefault()).toOffsetDateTime());

                return ResponseEntity.ok(response);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao processar estorno: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // VIOLAÇÃO [ISP] - Interface Segregation Principle
    // O controller é FORÇADO a implementar métodos da interface
    // PaymentReportOperations (relatórios, CSV, sincronização) que não têm
    // NENHUMA relação com sua responsabilidade de controller REST.
    // =========================================================================
    @Override
    public String generateMonthlyReport(int month, int year) {
        // Implementação forçada e vazia — controller não deveria gerar relatórios
        return "Relatório não implementado";
    }

    @Override
    public byte[] exportPaymentsToCSV() {
        // Implementação forçada e vazia — controller não deveria exportar CSV
        return new byte[0];
    }

    @Override
    public void syncPaymentsWithExternalSystem() {
        // Implementação forçada e vazia — controller não deveria sincronizar sistemas
    }
}