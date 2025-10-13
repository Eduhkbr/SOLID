# Integração com SonarCloud

## 1. Criação do Projeto no SonarCloud
- Acesse https://sonarcloud.io e faça login.
- Crie um novo projeto, anote o `projectKey`, `organization` e gere um `token` de autenticação.

## 2. Configuração do Maven (pom.xml)
No bloco `<build><plugins>` do seu pom.xml, adicione:
```xml
<plugin>
  <groupId>org.sonarsource.scanner.maven</groupId>
  <artifactId>sonar-maven-plugin</artifactId>
  <version>3.10.0.2594</version>
</plugin>
```

## 3. Execução da Análise
Execute na raiz do projeto:
```
mvn clean verify sonar:sonar ^
  -Dsonar.projectKey=SEU_PROJECT_KEY ^
  -Dsonar.organization=SEU_ORG ^
  -Dsonar.host.url=https://sonarcloud.io ^
  -Dsonar.login=SEU_TOKEN
```
(Substitua os valores conforme seu projeto. No Linux/Mac, use `\` ao invés de `^`.)

## 4. Dicas de Segurança
- Salve o token em uma variável de ambiente (SONAR_TOKEN) e use `-Dsonar.login=%SONAR_TOKEN%`.
- Nunca compartilhe o token em repositórios públicos.

## 5. Configuração opcional via arquivo
Você pode criar um arquivo `sonar-project.properties` na raiz do projeto com:
```
sonar.projectKey=SEU_PROJECT_KEY
sonar.organization=SEU_ORG
sonar.host.url=https://sonarcloud.io
```
Assim, basta rodar:
```
mvn clean verify sonar:sonar -Dsonar.login=SEU_TOKEN
```

## 6. Referências
- [Documentação SonarCloud](https://sonarcloud.io/documentation)
- [Plugin Maven Sonar](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)


