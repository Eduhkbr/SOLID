# syntax=docker/dockerfile:1.7

# ================= STAGE 1: Build =================
# Usa Maven e JDK para resolver dependências, compilar e testar a aplicação.
# Esta imagem é descartada; somente o artefato necessário segue para o runtime.
FROM maven:3.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do contêiner.
WORKDIR /app

# Copia o pom.xml antes do código para reaproveitar a etapa de dependências.
COPY pom.xml .

# Mantém as dependências no cache BuildKit entre builds e baixa as necessárias.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress dependency:go-offline

# Copia o código-fonte para o estágio de build.
COPY src ./src

# Compila o projeto e executa os testes antes de produzir o artefato.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress clean verify


# Baixa o agente OpenTelemetry em estágio isolado e confere o SHA-256 esperado.
# Fixar versão e checksum permite detectar mudanças inesperadas no artefato.
FROM scratch AS otel-agent
ADD --checksum=sha256:99a3bbd05d2ee93bcbd169de7814fa46c9dfdea61661deb806c09d0e1c288a85 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.4.0/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar


# ================= STAGE 2: Runtime =================
# Usa uma imagem JRE sem as ferramentas de compilação do estágio anterior.
FROM eclipse-temurin:21-jre-jammy

# Instala curl para os healthchecks e certificados necessários a conexões TLS.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup \
    && useradd --system --gid appgroup --home-dir /nonexistent appuser

# Define o diretório de trabalho.
WORKDIR /app

# Permite selecionar o nome do JAR produzido pelo build.
ARG JAR_FILE=target/*.jar

# Copia o JAR do estágio de build e define o proprietário sem camada extra de chown.
COPY --from=build --chown=appuser:appgroup /app/${JAR_FILE} /app/app.jar

# Copia o agente verificado para a imagem final.
COPY --from=otel-agent --chown=appuser:appgroup /opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Ativa a instrumentação automática do OpenTelemetry no processo Java.
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"

# Executa a aplicação com usuário sem privilégios.
USER appuser:appgroup

# Expõe a porta usada pela aplicação Spring Boot.
EXPOSE 8083

# Inicia o JAR diretamente para que o processo Java receba os sinais do contêiner.
ENTRYPOINT ["java", "-jar", "app.jar"]
