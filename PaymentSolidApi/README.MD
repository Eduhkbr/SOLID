# Instruções de Inicialização da Aplicação

Este projeto utiliza **OpenAPI Codegen** para gerar automaticamente os contratos da API. Por conta disso, é necessário seguir uma ordem específica de execução antes de subir a aplicação.

---

## Etapas obrigatórias antes de subir a aplicação

### 1. Rodar o build da plataforma com o perfil de teste

Antes de iniciar a aplicação, é **obrigatório compilar a plataforma** usando o perfil `test`. Isso garante que os arquivos gerados pelo OpenAPI Codegen estejam disponíveis, que o modulo de contratos compartilhados esteja resolvido e que o build seja concluído corretamente, sem exigir conexão com banco de dados.

```bash
./mvnw clean install -Dspring.profiles.active=test
```

### 2. Subir a aplicação
Após a compilação bem-sucedida, você pode iniciar a aplicação desde que o Docker esteja rodando.