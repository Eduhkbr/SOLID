# Acesso às Interfaces Web de Observabilidade

Como acessar e explorar as interfaces web e visualizações de observabilidade
do seu ecossistema, facilitando o entendimento e a análise do ambiente.

---

## 1. **Grafana** (`http://localhost:3000`)
- **Objetivo:** Painel central de visualização de métricas, logs.
- **Ações iniciais:**
  - Adicione as fontes de dados: Prometheus, Loki.
  - Importe dashboards prontos para PostgreSQL, RabbitMQ e Spring Boot.
  - Crie painéis customizados para logs.

## 2. **Prometheus** (`http://localhost:9090`)
- **Objetivo:** Consulta e exploração de métricas brutas.
- **Ações:**
  - Teste queries PromQL.
  - Explore as métricas coletadas dos serviços e exporters.

## 3. **Tempo (Tracing)** (`http://localhost:3200` / OTLP gRPC `localhost:4317`)
- **Objetivo:** Coletar e armazenar traces distribuídos gerados pelas aplicações.
- **Ações:**
  - Configure o Tempo como data source de tracing no Grafana (URL: `http://localhost:3200`).
  - Certifique-se que as aplicações enviem OTLP para o Tempo (gRPC na porta `4317`). No `docker-compose.yml` já há exemplo: `OTEL_EXPORTER_OTLP_ENDPOINT=http://tempo:4317` e `OTEL_EXPORTER_OTLP_PROTOCOL=grpc`.
  - Teste gerando uma requisição em uma aplicação (ex.: criar um pagamento) e então abra o Grafana → Explore → Traces ou um dashboard de tracing para visualizar as spans.
  - Para inspeção direta, a API HTTP do Tempo está disponível (por exemplo, para buscas de trace); porém a visualização costuma ser feita via Grafana.
- **Observações:**
  - A interface nativa do Tempo é limitada; use Grafana para uma experiência completa de visualização de traces.
  - Retenção e backend de armazenamento do Tempo dependem da configuração (file, S3, etc.).

## 4. **Alertmanager** (`http://localhost:9093`)
- **Objetivo:** Visualização e gerenciamento de alertas disparados.
- **Ações:**
  - Veja alertas ativos e históricos.
  - Teste o disparo de alertas críticos.

## 5. **Loki (via Grafana)**
- **Objetivo:** Visualização centralizada de logs dos containers.
- **Ações:**
  - No Grafana, acesse a aba "Explore" e selecione a fonte de dados Loki.
  - Filtre logs por serviço, container ou nível de severidade.

## 6. **RabbitMQ Management** (`http://localhost:15672`)
- **Objetivo:** Monitoramento e administração das filas e exchanges.
- **Ações:**
  - Visualize filas, conexões e throughput.
  - Realize operações administrativas.

## 7. **Nginx (Load Balancer) - Opcional**
- **Objetivo:** Não possui interface web nativa, mas logs e métricas podem ser visualizados via Loki/Grafana.

---

## **Resumo da Sequência Recomendada**
1. Grafana (painel central e configuração de fontes de dados)
2. Prometheus (exploração de métricas)
3. Tempo (tracing)
4. Alertmanager (alertas)
5. Loki (logs, via Grafana)
6. RabbitMQ Management (administração de filas)
