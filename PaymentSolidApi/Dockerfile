# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Mantém dependências no cache do BuildKit entre compilações.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress clean verify

# A versão é a usada pelo material da aula; o checksum impede baixar outro conteúdo.
FROM scratch AS otel-agent
ADD --checksum=sha256:99a3bbd05d2ee93bcbd169de7814fa46c9dfdea61661deb806c09d0e1c288a85 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.4.0/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup \
    && useradd --system --gid appgroup --home-dir /nonexistent appuser

WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build --chown=appuser:appgroup /app/${JAR_FILE} /app/app.jar
COPY --from=otel-agent --chown=appuser:appgroup /opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
USER appuser:appgroup
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
