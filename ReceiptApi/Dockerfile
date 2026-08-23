# ================= STAGE 1: Build =================
# Usa uma imagem completa do Maven e JDK para compilar o projeto.
# Esta imagem é grande, mas será descartada no final.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia os manifests primeiro para aproveitar o cache de dependencias.
COPY pom.xml ./
COPY solid-contracts/pom.xml solid-contracts/pom.xml
COPY PaymentSolidApi/pom.xml PaymentSolidApi/pom.xml
COPY ReceiptApi/pom.xml ReceiptApi/pom.xml

# Baixa as dependencias necessarias para o modulo de comprovantes e seus prerequisitos.
RUN mvn -pl ReceiptApi -am dependency:go-offline

# Copia o codigo-fonte necessario para o build multi-modulo.
COPY solid-contracts/src solid-contracts/src
COPY ReceiptApi/src ReceiptApi/src

# Compila o projeto e gera o arquivo .jar. O -DskipTests acelera o build.
RUN mvn -pl ReceiptApi -am clean install -DskipTests


# ================= STAGE 2: Runtime =================
# Usa uma imagem mínima, apenas com o Java Runtime Environment (JRE).
# Isso torna a imagem final muito menor e mais segura.
FROM eclipse-temurin:21-jre-jammy

# Instala ferramentas mínimas necessárias para healthchecks (curl) e certificados
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Cria um grupo e um usuário de sistema dedicados para a aplicação.
# --system cria um usuário sem home directory
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# Define o diretório de trabalho.
WORKDIR /app

# Argumento para o nome do JAR, permitindo que seja reutilizável.
ARG JAR_FILE=ReceiptApi/target/*.jar

# Copia o .jar gerado no STAGE 1 para a imagem final.
COPY --from=build /app/${JAR_FILE} app.jar

# Define a propriedade do diretório e do arquivo para o novo usuário.
# Isso garante que o usuário 'appuser' tenha permissão para acessar os arquivos.
RUN chown -R appuser:appgroup /app

# Muda o contexto de execução para o usuário não-root.
# A partir deste ponto, todos os comandos subsequentes serão executados como 'appuser'.
USER appuser

# Expõe a porta que a aplicação Spring Boot usa por padrão.
EXPOSE 8083

# Comando para executar a aplicação quando o contêiner iniciar.
ENTRYPOINT ["java", "-jar", "app.jar"]