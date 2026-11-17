# syntax=docker/dockerfile:1.7
# BUILD: JDK e Maven compilam/testam; não entram na imagem final.
# Tag facilita leitura; digest fixa conteúdo e exige atualização deliberada.
FROM maven:3.9.11-eclipse-temurin-21@sha256:6fdc855a6ed81d288ca7ca37ac6ff5e9308b612485c0801d70b25a858c83d237 AS build
WORKDIR /app
# POM antes do código preserva cache quando só uma classe muda.
# Cache mount guarda .m2 no builder: não vira camada final nem é cofre de segredos.
# sharing=locked evita disputa por .m2 em builds concorrentes.
COPY pom.xml ./
RUN --mount=type=cache,id=aula-maven,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress dependency:go-offline
# .dockerignore exclui target, credenciais e arquivos locais do contexto enviado.
COPY src ./src
# verify executa testes unitários. Integração PostgreSQL é obrigatória na suíte
# externa (-Pintegration), sem montar socket Docker dentro do build.
RUN --mount=type=cache,id=aula-maven,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress clean verify

# RUNTIME: JRE, certificados e curl utilizado no healthcheck; sem Maven/JDK.
FROM eclipse-temurin:21-jre-jammy@sha256:dff6eb46527bd293a3c06bbbe9e64e8c9f91f01905b5abbdad31761c03059d2c
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 appgroup \
    && useradd --system --uid 10001 --gid appgroup --home-dir /nonexistent appuser
WORKDIR /app
# Nome exato evita copiar JAR auxiliar por wildcard. Copiamos um JAR inteiro:
# cache Maven, camadas OCI e camadas internas do JAR são mecanismos diferentes.
COPY --from=build --chown=10001:10001 /app/target/PaymentSolidApi-0.0.1-SNAPSHOT.jar /app/app.jar
# UID/GID fixos sem root. Compose remove capabilities e usa raiz read-only.
USER 10001:10001
# EXPOSE só documenta; ports no Compose é que publica no host.
EXPOSE 8082
# Forma exec entrega SIGTERM ao Java (via init no Compose). Spring e prazo de
# parada devem ser compatíveis. JAVA_TOOL_OPTIONS é lido automaticamente pela JVM.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
