package br.com.fiap.PaymentSolidApi.adapter.in.controller;

import java.math.BigDecimal;
import java.sql.*;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.openapitools.model.PaymentRequestDTO;
import org.openapitools.model.ReceiptResponseDTO;
import org.openapitools.model.PaymentResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.fiap.PaymentSolidApi.PaymentStatus;
import br.com.fiap.PaymentSolidApi.Receipt;
import br.com.fiap.PaymentSolidApi.ReceiptRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pagamentos", description = "API de Gerenciamento de Pagamentos")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final DataSource dataSource;
    private final ReceiptRepository receiptRepository;

    public PaymentController(DataSource dataSource, ReceiptRepository receiptRepository) {
        this.dataSource = dataSource;
        this.receiptRepository = receiptRepository;
    }

    @Operation(
            summary = "Criar Novo Pagamento",
            description = "Processa um novo pagamento no sistema"
    )
    @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de pagamento inválidos")
    @PostMapping
    public ResponseEntity<String> process(@RequestBody PaymentRequestDTO request) {
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
                String receiptData = generateReceiptData(tipo, valor, pagamentoId.toString());
                try {
                    Receipt r = Receipt.builder()
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

    @Operation(
            summary = "Buscar um Comprovante de pagamento",
            description = "Busca um Comprovante de pagamento no sistema"
    )
    @ApiResponse(responseCode = "200", description = "Comprovante de pagamento encontrado com sucesso")
    @ApiResponse(responseCode = "204", description = "Comprovante de pagamento não encontrado")
    @GetMapping("/receipt/{id}")
    public ResponseEntity<ReceiptResponseDTO> findReceiptById(@PathVariable String id) {
        try {
            UUID paymentId = UUID.fromString(id);

            // Busca no MongoDB
            Optional<Receipt> receiptOptional = receiptRepository.findById(paymentId);
            if (receiptOptional.isPresent()) {
                Receipt receipt = receiptOptional.get();
                ReceiptResponseDTO responseDTO = new ReceiptResponseDTO();
                responseDTO.setReceiptData(receipt.getReceiptData());
                responseDTO.setPaymentId(receipt.getPaymentId());
                LocalDateTime createdAt = receipt.getCreatedAt();
                ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
                responseDTO.setCreatedAt(createdAt.atZone(zoneId).toOffsetDateTime());
                return ResponseEntity.ok(responseDTO);
            }

            // Busca no Postgres
            try (Connection conn = dataSource.getConnection()) {
                String sql = "SELECT * FROM PAYMENT_RECEIPTS WHERE PAYMENT_ID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setObject(1, paymentId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // VIOLAÇÃO SRP: Controller responsável por mapear o resultado do banco para um DTO.
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

    @Operation(
            summary = "Buscar um Pagamento",
            description = "Busca um pagamento no sistema"
    )
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso")
    @ApiResponse(responseCode = "204", description = "Pagamento não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> findPaymentById(@PathVariable String id) {
        try {
            // VIOLAÇÃO SRP e DIP: Lógica de acesso ao banco de dados direto no controller.
            // O controller agora é responsável por saber como se conectar e consultar o banco.

            // 4. Persistência de dados direto no controller (Violação Single Responsibility Principle)
            UUID pagamentoId = UUID.fromString(id);

            try (Connection conn = dataSource.getConnection()) {
                String sql = "SELECT ID, PAYMENT_METHOD, AMOUNT, STATUS, CREATED_AT FROM PAYMENTS WHERE ID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setObject(1, pagamentoId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // VIOLAÇÃO SRP: Controller responsável por mapear o resultado do banco para um DTO.
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


    // Violação Liskov Substitution Principle e Interface Segregation Principle:
    // Um endpoint de estorno que não se aplica a todos os tipos de pagamento
    @PostMapping("/refund/{id}")
    public ResponseEntity<String> refund(@PathVariable String id) {
        // Lógica de estorno...
        // Problema: PIX e Boleto não podem ser estornados da mesma forma que um Cartão.
        // O código aqui teria que fazer um "if" no tipo de pagamento para decidir o que fazer,
        // o que é um sintoma de violação do Liskov Substitution Principle.
        // LSP: Nem todos os tipos de pagamento podem ser estornados da mesma forma
        // PIX: não permite estorno
        // Boleto: precisa de cancelamento antes do vencimento
        // Cartão: permite estorno total
        // Isso força diferentes comportamentos para o mesmo metodo
        System.out.println("Estornando pagamento com ID: " + id);
        return ResponseEntity.ok("Pagamento estornado com sucesso.");
    }


    // Metodo para gerar dados do comprovante
    private String generateReceiptData(String tipo, BigDecimal valor, String pagamentoId) {
        // Exemplo simples de geração de comprovante
        return String.format(
                "COMPROVANTE DE PAGAMENTO\n" +
                        "ID: %s\n" +
                        "Tipo: %s\n" +
                        "Valor: R$ %.2f\n" +
                        "Data: %s\n" +
                        "Status: APROVADO",
                pagamentoId,
                tipo,
                valor,
                LocalDateTime.now()
        );
    }
}