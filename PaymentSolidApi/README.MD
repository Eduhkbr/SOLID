# Payment API

O laboratório e seus comandos vigentes estão no [guia da aula](../README.md). O build Docker executa geração OpenAPI e testes automaticamente; não é necessário um install manual anterior.

Contrato: src/main/resources/api/openapi.yml. Flyway: src/main/resources/db/migration/. Testes unitários: mvn verify. Testes PostgreSQL: mvn verify -Pintegration, somente com as variáveis e banco descartável da suíte.
