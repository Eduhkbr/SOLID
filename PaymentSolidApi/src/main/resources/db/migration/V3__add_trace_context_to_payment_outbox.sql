-- Preserva o contexto W3C do request para o relay assíncrono e o consumidor RabbitMQ.
ALTER TABLE PAYMENT_OUTBOX
    ADD COLUMN IF NOT EXISTS TRACE_CONTEXT JSONB;
