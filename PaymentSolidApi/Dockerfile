# syntax=docker/dockerfile:1.7
# ================= STAGE 1: Build =================
# Usa Maven e JDK para compilar e testar o projeto. Este estágio é descartado
# da imagem final, que contém apenas o runtime Java.
FROM maven:3.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do contêiner.
WORKDIR /app

# Copia o POM antes do código para reaproveitar as dependências quando o código
# mudar sem alteração no arquivo de dependências.
COPY pom.xml ./
# BuildKit mantém o repositório Maven em cache entre builds, sem copiá-lo para a
# imagem final.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress dependency:go-offline

# Copia o código-fonte do serviço.
COPY src ./src

# Compila o projeto, executa os testes e valida as verificações da fase verify.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress clean verify

# ================= STAGE 2: Runtime =================
# A imagem final usa somente o Java Runtime Environment; as ferramentas de
# build não são incluídas.
FROM eclipse-temurin:21-jre-jammy

# Instala o curl usado pelo healthcheck do Actuator e os certificados CA.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup \
    && useradd --system --gid appgroup --home-dir /nonexistent appuser

# Define o diretório de trabalho.
WORKDIR /app

# Argumento para permitir selecionar o JAR produzido pelo Maven.
ARG JAR_FILE=target/*.jar

# Copia somente o JAR da etapa de build e define a propriedade no próprio COPY.
COPY --from=build --chown=appuser:appgroup /app/${JAR_FILE} /app/app.jar

# Executa o processo com usuário sem privilégios.
USER appuser:appgroup

# Expõe a porta usada pela aplicação Spring Boot.
EXPOSE 8082

# Executa o JAR diretamente; opções adicionais da JVM podem ser passadas na
# configuração do contêiner.
ENTRYPOINT ["java", "-jar", "app.jar"]
