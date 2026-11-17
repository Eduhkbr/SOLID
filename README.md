# Conteinerização de aplicações Spring Boot

Laboratório local da aula: Payment recebe pagamentos fictícios e Receipt produz comprovantes por eventos RabbitMQ. Java 25, Spring Boot 4.1.2, Jackson 3, PostgreSQL 17 e RabbitMQ 4.3.6. Não há integração financeira nem autenticação de usuários. Use somente dados fictícios e mantenha as portas restritas ao computador local.

## Executar a aula

Requisitos: Docker Desktop com Engine Linux/WSL2 ativo e Docker Compose com suporte a `up --wait`. O build usa Maven/JDK dentro do contêiner; não exige Java ou Maven no Windows. O primeiro build precisa de Internet.

Na raiz do checkout, em PowerShell:

```powershell
$env:COMPOSE_PROJECT_NAME = 'aula-cont'
Copy-Item .env.example .env # Somente se ainda não existir; não sobrescreva suas configurações.
docker compose config --quiet
docker compose build payment-api
docker compose build receipt-api
docker compose up -d --wait --wait-timeout 180
docker compose ps
```

Payment: `http://127.0.0.1:8082`; Receipt: `http://127.0.0.1:8083`; RabbitMQ Management: `http://127.0.0.1:15672`. Ajuste as três portas no `.env` se outro laboratório já as utilizar. Banco e AMQP não são publicados no host. Credenciais do exemplo são fictícias; `.env` é ignorado pelo Git e pelo contexto de build.

```powershell
$body = @{ paymentMethod='BOLETO'; amount=42.50 } | ConvertTo-Json
$payment = Invoke-RestMethod http://127.0.0.1:8082/api/payments -Method Post -ContentType application/json -Body $body
$id = $payment.paymentId
Invoke-RestMethod "http://127.0.0.1:8082/api/payments/$id"
Invoke-RestMethod "http://127.0.0.1:8083/api/receipts/$id"
```

A criação retorna 201; a consulta do pagamento retorna 200 ou 404. Receipt retorna 204 enquanto o comprovante não existe e 200 quando disponível: aguarde a entrega assíncrona e consulte novamente. Swagger Payment: `/swagger-ui/index.html`, com contrato estático em `/openapi.yml`; Receipt usa `/swagger-ui/index.html` e `/v3/api-docs`.

## Contratos e consistência

- Valor positivo, no máximo duas casas decimais significativas e limite `99999999999999999.99`, compatível com `NUMERIC(19,2)`. Valores adicionais não são arredondados silenciosamente.
- CREDIT_CARD exige número fictício de 16 dígitos e CVV de 3 ou 4 dígitos; PIX exige chave fictícia. Dados de cartão não são gravados no banco nem enviados nos eventos/respostas. BOLETO e PIX rejeitam campos de cartão incompatíveis.
- O pagamento inicia em PENDING. BOLETO e CREDIT_CARD podem ser estornados uma vez para REFUNDED; PIX não pode ser estornado neste simulador. Repetição e conflito retornam 409. Não há vencimento de boleto nem liquidação financeira real.
- Pagamento e outbox são gravados na mesma transação local. O relay reserva um evento por vez, exige publisher confirm e ausência de retorno sem rota e repete com backoff. Uma tentativa antiga não sobrescreve a reserva de outra tentativa.
- Entrega é pelo menos uma vez. Receipt deduplica `eventId` e grava o comprovante na mesma transação; locks por pagamento no PostgreSQL coordenam JVMs diferentes. A versão impede regressão quando eventos chegam fora de ordem. O contrato atual tem criação v1 e estorno v2.
- O POST de criação não tem chave de idempotência HTTP: uma nova requisição pode criar outro pagamento. Não o repita automaticamente quando o resultado for desconhecido.
- A fila principal não expira mensagens por padrão. Falhas de processamento têm duas novas tentativas, além da tentativa inicial; o recoverer publica na DLQ com confirmação. A DLQ exige diagnóstico e reprocessamento deliberado.

## Docker e persistência

Os Dockerfiles são comentados para leitura em aula. O estágio de build executa `mvn clean verify`; o runtime recebe somente o JAR explícito. O cache `.m2` do BuildKit não faz parte da imagem final. Copiar um JAR inteiro não equivale a extrair suas camadas internas.

As APIs executam com o usuário `appuser` e o grupo `appgroup`, criados no Dockerfile. O Linux usa os números UID e GID para verificar permissões; ambos são fixados em 10001. `USER appuser:appgroup` escolhe essa conta por nome e `COPY --chown=appuser:appgroup` atribui a ela o JAR. A suíte confere nomes, números e propriedade do arquivo nos dois serviços.

As APIs usam raiz somente leitura, `/tmp` limitado, capabilities removidas, sem escalada de privilégios, limite de 768 MiB e 1 CPU por serviço. PostgreSQL e RabbitMQ também têm limites. Logs giram em três arquivos de até 10 MiB por serviço.

`depends_on` com `service_healthy` coordena a subida inicial. Readiness de Payment inclui banco; a indisponibilidade do broker é absorvida pelo outbox. Readiness de Receipt inclui banco e broker. Liveness não depende deles. `unhealthy` sozinho não reinicia um contêiner automaticamente. O Spring recebe SIGTERM com prazo de parada compatível; saída 143 pode acompanhar um shutdown normal da JVM, devendo ser interpretada junto aos logs.

Há uma instância PostgreSQL, dois bancos e dois papéis de aplicação sem superuser. Cada papel só acessa seu banco; Flyway cria/evolui as tabelas. O script de inicialização provisiona bancos e papéis somente no volume vazio. Alterar senhas no `.env` não altera automaticamente usuários de um volume existente.

Volumes preservam PostgreSQL e RabbitMQ após recriar contêineres; hostname estável preserva a identidade do nó RabbitMQ. Isso não fornece replicação, backup, restauração ou disponibilidade entre hosts.

**Migração do laboratório antigo:** use outro nome de projeto e volumes novos. Não monte automaticamente o volume antigo nesta revisão: o antigo modelo compartilhava banco, tinha inicialização de tabelas diferente e não possui o histórico Flyway V1 desta estrutura. Preserve dados antigos; uma migração de dados reais exige backup e procedimento próprio.

```powershell
docker compose stop        # Para e preserva contêineres e volumes.
docker compose down        # Remove contêineres/redes deste projeto; preserva volumes.
# RESET DESTRUTIVO SOMENTE DESTE LABORATÓRIO DESCARTÁVEL:
# docker compose down --volumes
```

Não use `docker system prune` ou remoção genérica de volumes para encerrar a aula.

## Verificação automatizada

O build normal executa testes unitários. `-Pintegration` acrescenta testes com PostgreSQL real via Failsafe; não usa mocks para comprovar SQL/locks e falha se a infraestrutura estiver ausente. O overlay `compose.tests.yml` fornece runners Maven limitados a 2 GiB, sem socket Docker. Os testes de integração usam e limpam tabelas: execute-os somente no projeto descartável criado pela suíte.

Para validar a jornada completa, instale Python 3.11+ para o script de regressão e execute PowerShell 7:

```powershell
./scripts/verify-lab.ps1 -Python python
```

No Linux/CI use `-Python python3`. O script cria nome exclusivo com prefixo `aula-verify-`, constrói imagens, executa as suítes sequencialmente e verifica HTTP, broker indisponível, publicação sem rota, duplicatas, concorrência de estorno, evento antigo, DLQ, persistência após recriação e SIGTERM. Asserções têm prazos; falhas produzem saída não zero. Relatórios JUnit ficam em `target/`; evidências HTTP ficam em `.validation/<projeto>/`. Ao terminar, remove somente contêineres, redes, volumes descartáveis e tags de imagem da própria execução. `-KeepResources` preserva esses recursos para diagnóstico.

O workflow `Laboratório de conteinerização` executa a mesma suíte em PRs. Sonar é uma verificação complementar: não substitui testes de runtime nem análise de dependências/imagens.

## Atualização e limites

Tags/digests das imagens constam nos Dockerfiles e Compose. Revise versões e vulnerabilidades antes de cada oferta da aula; um digest fixa conteúdo, não certifica segurança. A análise de segurança precisa distinguir ocorrência de pacote, configuração vulnerável e exploração demonstrada. Não extrapole testes locais para desempenho, HA ou segurança de produção.

Referências: [Dockerfile](https://docs.docker.com/reference/dockerfile/), [cache](https://docs.docker.com/build/cache/optimize/), [inicialização Compose](https://docs.docker.com/compose/how-tos/startup-order/), [volumes](https://docs.docker.com/engine/storage/volumes/), [migração Boot 4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide), [Jackson no Boot](https://docs.spring.io/spring-boot/reference/features/json.html), [conversão JSON AMQP](https://docs.spring.io/spring-amqp/reference/amqp/message-converters.html).

## Contrato Java 25

Build, testes e runtime usam Java 25, sem recursos preview. Os dois POMs compilam
com `--release 25` e o Enforcer recusa outro JDK na execução do Maven. Configure
também o SDK da IDE e o `JAVA_HOME` se executar Maven fora dos contêineres.
As bases JDK/JRE 25 são fixadas por digest. O runtime usa Ubuntu Noble.
Lombok é processador explícito nos dois módulos; Mockito entra como agente nos
testes unitários e de integração, sem depender de autoanexação dinâmica.
O BOM Spring Boot controla as versões transitivas. A suíte verifica a JVM efetiva
nas duas APIs, bytecode Java 25 e a jornada HTTP/OpenAPI/PostgreSQL/RabbitMQ.
