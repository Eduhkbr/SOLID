package br.com.fiap.PaymentSolidApi.infrastructure.adapter.in.controller;

/**
 * VIOLAÇÃO [ISP] - Interface Segregation Principle
 *
 * Interface "gorda" que mistura operações de relatório e integração externa.
 * Qualquer classe que a implemente é FORÇADA a conhecer e implementar
 * métodos que não precisa, como geração de relatórios e exportação de CSV.
 */
public interface PaymentReportOperations {

    String generateMonthlyReport(int month, int year);

    byte[] exportPaymentsToCSV();

    void syncPaymentsWithExternalSystem();
}

