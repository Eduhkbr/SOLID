package br.com.fiap.ReceiptApi.application.port.in;

import br.com.fiap.ReceiptApi.domain.vo.PaymentVO;

import java.util.UUID;

/** Coordena deduplicação e aplicação local do evento como uma única unidade de trabalho. */
public interface ProcessPaymentEventUseCase {
    void process(UUID eventId, long aggregateVersion, PaymentVO payment);
}
