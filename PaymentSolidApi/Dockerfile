# syntax=docker/dockerfile:1.7
# BUILD: JDK e Maven compilam/testam; não entram na imagem final.
# A tag é o nome legível da versão. O trecho @sha256:... é o digest:
# um identificador calculado a partir do conteúdo da base, para fixar essa revisão.
FROM maven:3.9.11-eclipse-temurin-25@sha256:407c4423cec0cf2981055bc2c6c0dc211d9605b6669279b95997f2d1c7e91e2c AS build
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
FROM eclipse-temurin:25-jre-noble@sha256:e3b6f7887a50f20d8f07abf279d8a81a5953fb398a9555bd9edd56b9bd15968c
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 appgroup \
    && useradd --system --uid 10001 --gid appgroup --home-dir /nonexistent appuser
WORKDIR /app
# Nome exato evita copiar JAR auxiliar por wildcard. Copiamos um JAR inteiro:
# cache Maven, camadas OCI e camadas internas do JAR são mecanismos diferentes.
COPY --from=build --chown=appuser:appgroup /app/target/PaymentSolidApi-0.0.1-SNAPSHOT.jar /app/app.jar
# appuser é o usuário da aplicação; appgroup é seu grupo. Ambos foram criados
# acima. UID e GID são os números usados pelo Linux para conferir permissões.
# Fixamos os dois em 10001 para manter a identidade entre builds, sem usar root.
# O nome torna a intenção legível. Compose também restringe escrita e privilégios.
USER appuser:appgroup
# EXPOSE só documenta; ports no Compose é que publica no host.
EXPOSE 8082
# Forma exec entrega SIGTERM ao Java (via init no Compose). Spring e prazo de
# parada devem ser compatíveis. JAVA_TOOL_OPTIONS é lido automaticamente pela JVM.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
