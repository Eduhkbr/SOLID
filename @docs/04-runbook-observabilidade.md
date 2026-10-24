# Runbook de mensageria, observabilidade e recuperação

Este runbook descreve o comportamento local do fluxo PaymentSolidApi → RabbitMQ → ReceiptApi e como verificar os sinais no Prometheus, Grafana e Alertmanager. A configuração Compose é didática e não representa, por si só, uma arquitetura de produção altamente disponível.

## Inicialização e estado dos serviços

Suba a stack com `docker compose up -d --build` e confira `docker compose ps`. Espere os healthchecks de PostgreSQL, RabbitMQ, PaymentSolidApi e ReceiptApi ficarem saudáveis antes dos testes. A API de pagamentos fica em `http://localhost:8082`; o Grafana, em `http://localhost:3000`; Prometheus, em `http://localhost:9090`; e Alertmanager, em `http://localhost:9093`.

Para consultar logs, use `docker compose logs --follow payment-api receipt-api rabbitmq`. Consulte um pagamento com `GET /api/payments/{id}` e seu comprovante com `GET /api/receipts/{paymentId}`. Métricas do outbox ficam em `GET /actuator/prometheus` no Payment.

## Outbox transacional e indisponibilidade do broker

O Payment persiste o pagamento e o evento em `PAYMENT_OUTBOX` na mesma transação PostgreSQL. Um relay busca eventos pendentes com `FOR UPDATE SKIP LOCKED`, publica uma mensagem persistente, espera o publisher confirm e só então marca o evento como publicado. Falhas recebem novas tentativas com atraso exponencial e jitter.

Para observar a recuperação em ambiente local:

1. Execute `docker compose stop rabbitmq`.
2. Crie um pagamento com `POST /api/payments`. O pagamento deve responder com sucesso mesmo com o broker indisponível.
3. Consulte `payment_outbox_pending` e `payment_outbox_oldest_pending_seconds` em `/actuator/prometheus`; o evento deve continuar pendente.
4. Execute `docker compose start rabbitmq` e aguarde o relay confirmar a publicação.
5. Confirme `published_at` em `PAYMENT_OUTBOX` e consulte o recibo pela API.

Uma confirmação do publisher informa que o broker aceitou a publicação; ela não prova que o consumidor aplicou o evento. A confirmação de consumo ocorre depois de o listener completar sua transação local.

## Redelivery e idempotência

Para demonstrar a fila durável, pare temporariamente o consumidor com `docker compose stop receipt-api`, crie um pagamento e confirme que o outbox foi publicado. Inicie o consumidor com `docker compose start receipt-api`; a mensagem deve permanecer disponível e produzir um comprovante.

O evento contém `eventId` e `aggregateVersion`. Receipt grava o `eventId` em `PROCESSED_EVENTS` na mesma transação que cria ou atualiza o comprovante. Uma reentrega com o mesmo identificador não repete o efeito de domínio. O `aggregateVersion` identifica a sequência do estado do pagamento; a chave única no outbox impede duas intenções para a mesma versão.

## Retries e dead-letter queue

Receipt aplica até três tentativas com backoff exponencial. Ao esgotar as tentativas, `RepublishMessageRecovererWithConfirms` publica a mensagem na exchange `payment.exchange.dlx`; o binding a encaminha para `payment.receipt.dlq`. O TTL da fila principal também está configurado para dead-letter após expiração.

Consulte quantidade e estado da DLQ pela API de gerenciamento do RabbitMQ ou na métrica `rabbitmq_queue_messages{queue="payment.receipt.dlq"}`. Inspecione headers `x-exception-message`, `x-exception-stacktrace`, `x-original-exchange` e `x-original-routingKey` antes de corrigir ou reenviar uma mensagem. Faça redrive de forma controlada, após corrigir a causa, com limite de lote e deduplicação habilitada. Não reenvie em massa mensagens inválidas: isso pode criar um ciclo de falha e aumentar a fila.

## Alertas, agrupamento e recuperação

O Prometheus avalia as regras em `prometheus/alert.rules.yml`. O Alertmanager agrupa por `alertname`, `job` e `severity`; assim, réplicas do mesmo serviço formam uma notificação, enquanto instâncias diferentes continuam visíveis dentro do grupo. `group_wait`, `group_interval` e `repeat_interval` controlam atraso inicial, novas mudanças do grupo e repetição.

O Grafana provisiona as fontes Prometheus e Alertmanager. Consulte alertas em **Alerting → Alert rules** e grupos/estado em **Alerting → Alert groups**. O receiver `aula-webhook` é local e grava notificações firing e resolved em `alert_notifications`; não envia e-mail ou mensagens externas. Em produção, configure um receiver apropriado, autenticação, TLS e política de deduplicação.

Para validar regras antes de alterar a configuração:

```bash
docker compose exec prometheus promtool check rules /etc/prometheus/alert.rules.yml
docker compose exec prometheus promtool test rules /etc/prometheus/tests/alert.rules.test.yml
```

O segundo comando verifica que o alerta de CPU aparece depois do período `for` para as duas réplicas e desaparece quando a série normaliza. O Alertmanager agrupa as duas instâncias, envia firing após `group_wait` e envia resolved quando ambas recuperam.

## Investigação e recuperação

1. Identifique o alerta e o serviço/instância afetados no Grafana.
2. Para backlog, compare `payment_outbox_pending` com `payment_outbox_oldest_pending_seconds`; verifique conectividade e confirmações do RabbitMQ.
3. Para recibos ausentes, confirme mensagem na fila principal ou DLQ, retries e registros em `PROCESSED_EVENTS`.
4. Correlacione logs com `traceId`/`spanId` quando a exportação de traces estiver ativa. Evite transformar IDs de alta cardinalidade em labels do Loki.
5. Depois de restaurar o serviço, observe a redução do backlog e o alerta resolved; registre a causa e os dados que sustentam o diagnóstico.

`docker compose down` encerra os serviços e preserva os volumes. `docker compose down -v` também apaga dados persistidos; use apenas quando o objetivo for reinicializar explicitamente este laboratório.
