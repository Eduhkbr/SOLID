# syntax=docker/dockerfile:1.7
# ================= Estágio de build =================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Mantém o cache de dependências entre builds sem copiá-lo para a imagem final.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src

# A imagem só é produzida depois que os testes do projeto passam.
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress clean verify

# ================= Estágio de runtime =================
FROM eclipse-temurin:21-jre-jammy

# O Compose usa curl no healthcheck do Actuator.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup \
    && useradd --system --gid appgroup --home-dir /nonexistent appuser

WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build --chown=appuser:appgroup /app/${JAR_FILE} /app/app.jar

USER appuser:appgroup
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
