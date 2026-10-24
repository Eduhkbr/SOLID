package br.com.fiap.ReceiptApi.application.port.out;

import java.util.UUID;

/** Registra a identidade do evento e informa se esta chamada obteve o direito de aplicá-lo. */
public interface ProcessedEventRepository {
    boolean registerIfAbsent(UUID eventId, UUID paymentId, long aggregateVersion);
}
