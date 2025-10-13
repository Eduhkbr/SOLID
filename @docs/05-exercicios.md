# Guia Didático: Ecossistema, Arquitetura e Observabilidade em Microsserviços

## Lição 1: Visão Geral do Ecossistema

### O que é um Ecossistema de Microsserviços?
Um ecossistema de microsserviços é um conjunto de serviços independentes que trabalham juntos para formar uma aplicação maior. Cada serviço é responsável por uma funcionalidade específica, permitindo escalabilidade, manutenção e deploy independentes.

**Exemplo Prático:** Imagine uma loja online. Um microsserviço gerencia usuários, outro processa pagamentos, e um terceiro cuida de inventário. Eles se comunicam via mensagens ou APIs.

### Nosso Caso de Estudo: Sistema de Pagamentos e Comprovantes
Este projeto simula um sistema de pagamentos onde:
- Usuários fazem pagamentos via API.
- Eventos são processados assincronamente.
- Dados são persistidos em um banco relacional.
- Tudo é monitorado para garantir disponibilidade.

---

## Lição 2: Arquitetura dos Componentes

### 1. Microsserviços
- **payment-api**: Processa pagamentos. Expõe endpoints REST e publica eventos.
- **receipt-api**: Gera comprovantes. Consome eventos e expõe APIs.

**Exercício 1:** Acesse http://localhost:8082/actuator/health

### 2. Banco de Dados
- **postgres**: Armazena dados de forma relacional. Inicializado com scripts SQL.

**Exercício 2:** Execute `docker compose logs postgres` e identifique as mensagens de inicialização.

### 3. Mensageria
- **rabbitmq**: Permite comunicação assíncrona, evitando dependências síncronas.

**Pergunta:** Qual a diferença entre comunicação síncrona e assíncrona?

### 4. Balanceamento de Carga
- **nginx-lb**: Distribui requisições entre instâncias para alta disponibilidade.

### 5. Observabilidade
- **prometheus**: Coleta métricas.
- **grafana**: Visualiza métricas.
- **alertmanager**: Gerencia alertas.
- **postgres-exporter** e **rabbitmq-exporter**: Exportam métricas específicas.
- **loki** e **promtail**: Gerenciam logs.

**Exercício 3:** Acesse http://localhost:9090/targets

### 6. Infraestrutura
- **Volumes**: Persistem dados.
- **Redes**: Isolam comunicação.

---

## Lição 3: Fluxo de Dados e Comunicação

### Como os Dados Fluem?
1. Usuário → nginx-lb → payment-api ou receipt-api.
2. payment-api → PostgreSQL + RabbitMQ.
3. receipt-api ← RabbitMQ → PostgreSQL.
4. Métricas → Prometheus → Alertmanager.
5. Logs → Promtail → Loki → Grafana.

**Diagrama Interativo:**
```
Usuário
  |
[nginx-lb]
  |         \
[payment-api]  [receipt-api]
   |   |         |   |
 [PostgreSQL]  [RabbitMQ]
   |   |         |   |
[postgres-exporter] [rabbitmq-exporter]
   |   |         |   |
 [Prometheus]---[Alertmanager]
   |   |         | 
 [Grafana]   [Loki]
                |
            [Promtail]
```

**Exercício 4:** Via postman faça uma chamada em um endpoint do payment-api e observe os logs no RabbitMQ (http://localhost:15672).

---

## Lição 4: Observabilidade

### Monitoramento
Prometheus coleta métricas (ex: uso de CPU, latência). Grafana cria dashboards.

**Exercício 5:** No Grafana (http://localhost:3000, user: admin, pass: admin).

### Alertas
Alertmanager exibe alertas baseados em regras (ex: serviço down).

**Exercício 6:** Pare o receipt-api (`docker compose stop receipt-api`) e veja o alerta em http://localhost:9093.

### Logs
Loki centraliza logs para busca rápida.

**Exercício 7:** No Grafana, explore a seção "Explore" e busque logs do payment-api.
1. Acesse http://localhost:3000 (usuário: admin, senha: admin).
2. No menu lateral esquerdo, clique em "Explore" (ícone de bússola).
3. No topo da página, selecione a fonte de dados "Loki" (se não estiver selecionada).
4. Clique em Add query, selecione em label filters "job" = "docker"
5. Clique em "Run query" para executar e visualizar os logs.
6. Se não aparecer nada, verifique se o Loki está rodando (`docker compose ps loki`) e se há logs recentes no container (`docker compose logs promtail`).

## Benefícios e Desafios

### Benefícios
- **Escalabilidade:** Escalone serviços individualmente.
- **Resiliência:** Falhas isoladas.
- **Observabilidade:** Monitoramento completo.

### Desafios
- **Complexidade:** Mais componentes para gerenciar.
- **Latência:** Comunicação entre serviços.
