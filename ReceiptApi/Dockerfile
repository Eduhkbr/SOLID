# syntax=docker/dockerfile:1.7

# ================= STAGE 1: Build =================
# Usa uma imagem completa do Maven e JDK para compilar o projeto.
# Esta imagem é grande, mas será descartada no final.
FROM maven:3.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do contêiner.
WORKDIR /app

# Copia o pom.xml primeiro para aproveitar o cache de camadas do Docker.
# Se as dependências não mudarem, esta camada não será re-executada.
COPY pom.xml .

# Baixa todas as dependências do projeto.
# BuildKit mantém as dependências em cache entre compilações.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress dependency:go-offline

# Copia o restante do código-fonte.
COPY src ./src

# Compila o projeto, gera o arquivo .jar e executa os testes.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress clean verify


# Baixa o agente OpenTelemetry em estágio isolado e valida seu SHA-256.
# A versão e o checksum fixos tornam explícito o artefato externo utilizado.
FROM scratch AS otel-agent
ADD --checksum=sha256:99a3bbd05d2ee93bcbd169de7814fa46c9dfdea61661deb806c09d0e1c288a85 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.4.0/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar


# ================= STAGE 2: Runtime =================
# Usa uma imagem mínima, apenas com o Java Runtime Environment (JRE).
# Isso torna a imagem final muito menor e mais segura removendo ferramentas de build.
FROM eclipse-temurin:21-jre-jammy

# Instala ferramentas mínimas necessárias para healthchecks (curl) e certificados
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Cria um grupo e um usuário de sistema dedicados para a aplicação.
# --system cria um usuário sem home directory
RUN groupadd --system appgroup && \
    useradd --system --gid appgroup --home-dir /nonexistent appuser

# Define o diretório de trabalho.
WORKDIR /app

# Argumento para o nome do JAR, permitindo que seja reutilizável.
ARG JAR_FILE=target/*.jar

# Copia o .jar gerado no STAGE 1 para a imagem final.
COPY --from=build --chown=appuser:appgroup /app/${JAR_FILE} /app/app.jar

# Baixa e verifica o agente OpenTelemetry para instrumentação automática.
COPY --from=otel-agent --chown=appuser:appgroup /opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Configura a variável de ambiente para ativar o agent
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"

# Define a propriedade do diretório e do arquivo para o novo usuário.
# Isso garante que o usuário 'appuser' tenha permissão para acessar os arquivos.
# Muda o contexto de execução para o usuário não-root.
# A partir deste ponto, todos os comandos subsequentes serão executados como 'appuser'.
USER appuser:appgroup

# Expõe a porta que a aplicação Spring Boot usa por padrão.
EXPOSE 8083

# Comando para executar o contêiner quando iniciar.
# Executa JAR diretamente com Java
# Para opções da JVM, use JAVA_TOOL_OPTIONS.
ENTRYPOINT ["java", "-jar", "app.jar"]
