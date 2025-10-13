# Runbook Básico: Observabilidade dos Serviços PaymentSolidApi e ReceiptApi

## 1. Coleta de Dados

- Certifique-se de que os serviços **PaymentSolidApi** e **ReceiptApi** estejam exportando métricas (Prometheus), logs (Loki) e traces (Tempo).
- Verifique se os agentes (Promtail, OpenTelemetry Collector, etc.) estão rodando e enviando dados corretamente para esses serviços.
- Comandos úteis:
  - Verificar status dos containers:
    - `docker ps | findstr PaymentSolidApi`
    - `docker ps | findstr ReceiptApi`
  - Verificar logs de um serviço:
    - `docker logs <nome-do-container-PaymentSolidApi>`
    - `docker logs <nome-do-container-ReceiptApi>`

## 2. Visualização

- Acesse o Grafana (`http://localhost:3000` ou URL definida).
- Confira dashboards específicos:
  - **PaymentSolidApi**: métricas de uptime, latência, erros, volume de pagamentos processados.
  - **ReceiptApi**: métricas de uptime, latência, erros, volume de recibos gerados.
  - Dashboards de logs e traces para análise detalhada de cada serviço.

## 3. Alertas

- Verifique se o Alertmanager está configurado e recebendo alertas do Prometheus para os serviços **PaymentSolidApi** e **ReceiptApi**.
- Teste um alerta forçando uma condição (ex: parar o container de um dos serviços).
- Confirme o recebimento do alerta (e-mail, Slack, etc.).

## 4. Investigação de Incidentes

- Ao receber um alerta relacionado ao **PaymentSolidApi** ou **ReceiptApi**:
  1. Consulte o dashboard correspondente no Grafana.
  2. Analise logs no Loki filtrando pelo serviço afetado.
  3. Utilize traces no Tempo para rastrear a origem do problema entre os serviços.

## 5. Resposta e Mitigação

- Siga o procedimento padrão para mitigar o incidente:
  - Reiniciar o container do serviço afetado: `docker restart <nome-do-container>`
  - Escalar para a equipe responsável, se necessário.
- Documente a causa raiz e as ações tomadas.

## 6. Pós-Incidente

- Revise dashboards e alertas dos serviços **PaymentSolidApi** e **ReceiptApi** para identificar possíveis melhorias.
- Atualize o runbook conforme necessário.