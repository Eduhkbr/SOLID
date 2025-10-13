# Dashboards no Grafana

## 1. Importando Dashboards Oficiais

1. Acesse o Grafana em http://localhost:3000 (usuário: admin, senha: admin ou a definida).
2. No menu lateral, clique em “+” > “Import”.
3. No campo “Import via grafana.com”, insira o código do dashboard desejado:
   - **PostgreSQL**: 9628 ([link](https://grafana.com/grafana/dashboards/9628))
   - **RabbitMQ**: 10991 ([link](https://grafana.com/grafana/dashboards/10991))
   - **Spring Boot**: 10280 ([link](https://grafana.com/grafana/dashboards/10280))
4. Clique em “Load”.
5. Selecione a fonte de dados Prometheus e clique em “Import”.
6. O dashboard aparecerá no menu inicial do Grafana.

---

## Dicas
- Você pode exportar e importar dashboards usando o menu de opções do dashboard.
- Compartilhe o JSON do dashboard para replicar em outros ambientes.
- Explore o Grafana Labs para mais dashboards prontos: https://grafana.com/grafana/dashboards

---

**Resumo:**
- Importe dashboards prontos para PostgreSQL, RabbitMQ e Spring Boot.
- Crie dashboards customizados conforme a necessidade do seu time.
- Use queries PromQL e logs Loki para visualização completa.