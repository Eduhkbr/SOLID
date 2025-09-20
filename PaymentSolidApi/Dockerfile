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
RUN mvn dependency:go-offline

# Copia o restante do código-fonte.
COPY src ./src

# Compila o projeto e gera o arquivo .jar. O -DskipTests acelera o build.
RUN mvn clean install -DskipTests


# ================= STAGE 2: Runtime =================
# Usa uma imagem mínima, apenas com o Java Runtime Environment (JRE).
# Isso torna a imagem final muito menor e mais segura removendo ferramentas de build
FROM eclipse-temurin:21-jre-jammy

# Define o diretório de trabalho.
WORKDIR /app

# Argumento para o nome do JAR, permitindo que seja reutilizável.
ARG JAR_FILE=target/*.jar

# Copia o .jar gerado no STAGE 1 para a imagem final.
COPY --from=build /app/${JAR_FILE} app.jar

# Expõe a porta que a aplicação Spring Boot usa por padrão.
EXPOSE 8080

# Definição do ponto de entrada da aplicação
# Executa JAR diretamente com Java
# Permite passagem de argumentos JVM se necessário
ENTRYPOINT ["java", "-jar", "app.jar"]