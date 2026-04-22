# Observabilidade para Microsserviços

## Introdução: Por que Observabilidade?

## Os Três Pilares da Observabilidade
- **Logs:** Registro detalhado de eventos. Úteis para análise de causa raiz.
- **Métricas:** Números agregados ao longo do tempo (latência, uso de CPU, contagem de requisições). 
Facilitam identificar tendências e anomalias.
- **Rastreamentos (Traces):** Acompanham o caminho de uma requisição por vários serviços, identificando gargalos e latências.

## Componentes da Stack de Observabilidade
- **Prometheus:** Coleta e armazena métricas de serviços.
- **Loki:** Armazena e indexa logs de containers.
- **Promtail:** Agente que coleta logs dos containers e envia para o Loki.
- **Grafana:** Painel unificado para visualizar métricas, logs e rastreamentos.
- **Alertmanager:** Gerencia alertas baseados em regras do Prometheus.
- **Exporters:** Exportam métricas específicas (ex: postgres-exporter, rabbitmq-exporter).

## Roteiro Prático: Workshop de Observabilidade

### 1. Preparação do Ambiente
- Instale Docker Desktop.
- Clone o repositório do projeto.
- Estruture os arquivos de configuração (docker-compose.yml, prometheus.yml, loki-config.yaml, promtail-config.yaml, etc).

### 2. Subindo a Stack
```bash
docker compose up -d
```
- Isso inicia todos os serviços (APIs, banco, mensageria, Prometheus, Grafana, Loki, etc).
- Verifique se tudo está rodando:
```bash
docker compose ps
```
- Acesse as interfaces:
  - Grafana: http://localhost:3000 (admin/admin)
  - Prometheus: http://localhost:9090

### 3. Gerando e Observando Dados
- Interaja com as APIs (ex: curl em endpoints REST).
- Veja logs em tempo real:
```bash
docker compose logs -f payment-api
```
- No Prometheus, veja métricas e alvos em "Status > Targets".
- No Grafana, crie painéis para visualizar métricas (Prometheus) e logs (Loki).

### 4. Exercícios e Exploração
- Crie métricas customizadas com Micrometer no código.
- Simule falhas e veja alertas no Alertmanager.
- Explore queries no Prometheus e no Grafana.

---

Objetivos da aula
- Entender o que é observabilidade e por que ela importa em um ecossistema de microsserviços.
- Ver quais métricas e sinais observar (métricas, logs, traces e healthchecks).
- Aprender a expor métricas em aplicações Spring Boot (Micrometer + Actuator).
- Fazer a integração básica com Prometheus, Grafana e Alertmanager.
- Ver exemplos de regras de alerta, boas práticas e exercícios práticos.

Resumo do ecossistema (caso de estudo)
- payment-api: processa pagamentos, expõe endpoints REST e publica eventos.
- receipt-api: gera comprovantes, consome eventos e expõe APIs.
- Postgres: banco relacional, inicializado por scripts.
- RabbitMQ: mensageria para comunicação assíncrona.
- nginx (balanceador): distribui tráfego entre instâncias.
- Prometheus, Grafana, Alertmanager: observabilidade.
- postgres-exporter, rabbitmq-exporter: exportadores de métricas específicas.
- Loki + Promtail: agregação e busca de logs.

Fluxo de dados (visão simplificada)
1. Usuário → nginx-lb → payment-api ou receipt-api
2. payment-api → Persiste em PostgreSQL + publica em RabbitMQ
3. receipt-api ← consome RabbitMQ → persiste em PostgreSQL
4. Prometheus coleta métricas via endpoints de cada serviço
5. Logs são enviados por Promtail para o Loki e consultados no Grafana

Por que isso importa?
- Microssserviços criam observabilidade distribuída: não basta logs locais; 
precisamos métricas agregadas, alertas e busca de logs centralizada para depurar e operar em produção.

1. Fundamentos e pré‑requisitos
- Cada microsserviço deve expor um endpoint Prometheus (por exemplo: /actuator/prometheus) — 
isso é feito com Spring Boot Actuator + Micrometer.
- Ter os serviços orquestrados (no projeto usamos Docker Compose) facilita o ambiente local da aula.
- Garantir que todos os serviços de observabilidade estejam na mesma rede Docker que as aplicações.

2. Métricas essenciais (o que monitorar)
2.1 Pool de conexões
- Conexões ativas, disponíveis e em uso (HikariCP, Tomcat JDBC).
- Latência de obtenção de conexão.

2.2 Chamadas HTTP
- Contagem de requisições por endpoint.
- Latência média, máxima e percentis (p95, p99).
- Taxa de erros (4xx e 5xx).

2.3 Disponibilidade
- Healthchecks (actuator/health) e métrica up do Prometheus.
- Reinicializações de containers e falhas de inicialização.

2.4 Recursos de infraestrutura
- Uso de CPU e memória (JVM e container).
- Garbage Collection: contagem e tempo gasto.
- Espaço em disco.

2.5 Outros itens
- Filas (RabbitMQ): comprimento da fila, mensagens não processadas.
- Banco de dados: queries lentas, locks e deadlocks.

3. Expondo métricas nos microsserviços (prático)
- Dependências (pom.xml): adicionar Actuator e Micrometer Prometheus registry.

  Trecho recomendado para o `pom.xml`:

  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
  ```

- Configurações sugeridas no `application.properties` (ou `application.yml`):

  ```properties
  management.endpoints.web.exposure.include=*
  management.endpoint.prometheus.enabled=true
  management.metrics.export.prometheus.enabled=true
  management.health.db.enabled=true
  management.health.rabbit.enabled=true
  management.metrics.enable.jvm=true
  management.metrics.enable.tomcat=true
  management.metrics.enable.hikaricp=true
  ```

- Métricas customizadas: use o Micrometer no código para criar contadores, timers e gauges que reflitam métricas de negócio.

Exercício rápido (local): verificar /actuator/prometheus de uma aplicação (substitua a porta quando necessário):

```bash
curl http://localhost:8082/actuator/prometheus | more
```

4. Integração inicial: Prometheus e Grafana
4.1 Prometheus
- Adicione Prometheus ao `docker-compose.yml` e crie `prometheus.yml` com jobs apontando para os endpoints das aplicações.
- Valide targets em: http://localhost:9090/targets

4.2 Grafana
- Adicione Grafana ao `docker-compose.yml`.
- Configure o Prometheus como data source.
- Importe dashboards prontos para Spring Boot/Micrometer, JVM, Docker e RabbitMQ (existem muitos dashboards oficiais na comunidade Grafana).

Comandos úteis (examples):

```bash
# Verificar targets do prometheus (local):
start http://localhost:9090/targets

# Acessar grafana na máquina local (usualmente):
start http://localhost:3000
```

5. Alertas e Alertmanager
- Adicione Alertmanager no `docker-compose.yml` e configure `alertmanager.yml` com rotas e receptores (Slack, email, webhook, etc.).
- Configure Prometheus para usar o Alertmanager.

Exemplo de regras (arquivo `alert.rules.yml` ou dentro do `prometheus.yml`):

```yaml
groups:
  - name: alertas_essenciais
    rules:
      - alert: ServicoIndisponivel
        expr: up{job="payment-api"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Instância do payment-api indisponível"
          description: "O serviço payment-api está fora do ar há mais de 1 minuto."

      - alert: PoolConexoesExaurido
        expr: hikari_connections_active{job="payment-api"} / hikari_connections_max{job="payment-api"} > 0.9
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Pool de conexões quase esgotado"
          description: "Mais de 90% das conexões do pool estão em uso."

      - alert: ErroHttpElevado
        expr: rate(http_server_requests_seconds_count{job="payment-api",status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Taxa de erro HTTP 5xx elevada"
          description: "Mais de 5% das requisições estão retornando erro 5xx."

      - alert: UsoCpuElevado
        expr: process_cpu_seconds_total{job="payment-api"} / (process_start_time_seconds{job="payment-api"} + 1) > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Uso de CPU elevado"
          description: "Uso de CPU acima de 80% por mais de 5 minutos."

      - alert: UsoMemoriaElevado
        expr: jvm_memory_used_bytes{area="heap",job="payment-api"} / jvm_memory_max_bytes{area="heap",job="payment-api"} > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Uso de memória heap elevado"
          description: "Mais de 85% da heap JVM está em uso."
```

Observação: ajuste as expressões (expr) e os nomes dos jobs conforme seus `scrape_configs` no `prometheus.yml`.

6. Logs com Loki e Promtail
- Use Promtail para enviar logs dos containers para o Loki.
- No Grafana, configure Loki como data source e use a aba Explore para buscar logs por labels.

Exercício prático (buscar logs no Grafana):
1. Acesse http://localhost:3000 (usuário: admin, senha: admin).
2. Clique em "Explore".
3. Selecione a fonte Loki.
4. Use expressões de labels baseadas nos nomes dos serviços do Docker Compose:

**Queries Loki - Exemplos prontos para usar:**

```logql
# Ver todos os logs da payment-api
{service="payment-api"}
# Inclua em + Operations: Line contains e o paymentId, ex: 22608a54-5f36-49df-abc8-32d716c088e3

# Ver todos os logs da receipt-api
{service="receipt-api"}

# Filtrar por aplicacao (mesmo valor que service)
{application="payment-api"}

# Ver logs do PostgreSQL
{service="postgres-payments"}

# Ver logs do RabbitMQ
{service="rabbitmq"}

# Ver logs do Nginx
{service="nginx-lb"}

# Filtrar logs que contenham ERROR ou EXCEPTION
{service="payment-api"} |~ "(?i)error|exception"

# Filtrar por nivel de log (extraido do JSON do logback)
{service="payment-api"} | json | level="ERROR"

# Buscar por traceId especifico nos logs
{service="payment-api"} |~ "traceId"

# Contar erros por servico nos ultimos 5 minutos
sum(count_over_time({service=~"payment-api|receipt-api"} |~ "(?i)error" [5m])) by (service)

# Ver todos os containers do projeto
{project="solid"}

# Filtrar por container_name especifico (util com replicas)
{container_name=~"solid-payment-api.*"}
```

**Queries Prometheus - Exemplos prontos para usar no Explore:**

```promql
# Verificar quais servicos estao UP
up{job=~"payment-api|receipt-api"}

# Taxa de requisicoes por segundo
sum(rate(http_server_requests_seconds_count{job="payment-api"}[1m])) by (uri, method)

# Latencia P95 em milissegundos
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="payment-api"}[5m])) by (le)) * 1000

# Taxa de erros 5xx
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (job)

# Uso de heap JVM (percentual)
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}

# Conexoes ativas no HikariCP
hikaricp_connections_active

# CPU do processo
process_cpu_usage{job=~"payment-api|receipt-api"}

# Threads da JVM
jvm_threads_live_threads

# Mensagens nas filas RabbitMQ
rabbitmq_queue_messages

# Transacoes por segundo no PostgreSQL
rate(pg_stat_database_xact_commit[1m])
```

**Queries Tempo - Exemplos de busca de traces:**

No Explore, selecione o datasource Tempo e use a aba "Search":
- Service Name: `PaymentSolidApi` ou `ReceiptApi`
- Span Name: `GET /api/payments` ou `POST /api/payments`
- Min Duration: `100ms` (para encontrar requisicoes lentas)

7. Boas práticas para produção
- Persistência: monte volumes para Prometheus, Alertmanager, Grafana (dashboards) e Loki.
- Segurança: proteja as interfaces web (autenticação, rede, TLS) e limite o acesso à API do Prometheus/Grafana.
- Alertas inteligentes: ajuste thresholds para reduzir falsos positivos e agrupe alertas relacionados.
- Backup: exporte configurações e dashboards do Grafana e faça backup das regras do Prometheus e do Alertmanager.
- Documentação: mantenha inventário de métricas, significado de cada alerta e playbooks de resposta.

8. Estrutura sugerida de diretórios para projeto didatico
```
prometheus/
  alert.rules.yml
  alertmanager.yml
  loki-config.yml
  prometheus.yml
  promtail-config.yml
```

9. Referências rápidas
- Prometheus: https://prometheus.io/docs/
- Grafana: https://grafana.com/docs/
- Alertmanager: https://prometheus.io/docs/alerting/latest/alertmanager/
- Spring Boot Actuator: https://docs.spring.io/spring-boot/docs/current/actuator-api/htmlsingle/
- Micrometer: https://micrometer.io/docs


Comandos úteis

```bash
# Subir todo o ambiente (no diretório do projeto):
docker compose up -d

# Ver logs de um serviço (ex.: postgres):
docker compose logs postgres

# Parar um serviço (ex.: receipt-api):
docker compose stop receipt-api

# Verificar se o prometheus está coletando targets:
start http://localhost:9090/targets

# Acessar grafana no browser:
start http://localhost:3000
```

Conteúdo baseado em práticas recomendadas do ecossistema Prometheus/Grafana/Alertmanager,
e adaptado para o contexto deste projeto didático.