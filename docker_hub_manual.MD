# Publicando sua API Spring Boot no Docker Hub

## Por que publicar no Docker Hub?

Quando desenvolvemos uma aplicação em containers, precisamos de um lugar para armazenar nossas imagens Docker. É aí que entra o Docker Hub - imagine ele como o GitHub, mas para imagens Docker.

Publicar sua imagem permite que qualquer pessoa (incluindo você mesmo em outro computador ou um servidor de produção) baixe e execute sua aplicação com apenas um comando.

## O que você vai precisar

- **Docker Desktop** rodando na sua máquina
- **Uma conta no Docker Hub** (gratuita em [hub.docker.com](https://hub.docker.com))
- **Sua aplicação Spring Boot** já com Dockerfile configurado

---

## Parte 1: Preparando a imagem

### Construindo com Docker Compose

Se você tem um `docker-compose.yml` no seu projeto (como nosso payment-api), essa é a forma mais simples:

```bash
# Na pasta raiz do projeto
docker-compose build payment-api
```

### Verificando se deu certo

Depois do build, confira se a imagem foi criada:

```bash
docker images
```

Procure por algo como `solid-payment-api` na lista. O nome geralmente segue o padrão: `nome-da-pasta_nome-do-serviço`.

---

## Parte 2: Conectando ao Docker Hub

### Fazendo login

Abra o terminal e execute:

```bash
docker login
```

Digite seu usuário e senha do Docker Hub quando solicitado.

> **Dica de segurança:** Em vez de usar sua senha principal, crie um Access Token nas configurações de segurança do Docker Hub. É mais seguro, especialmente se você trabalha em equipe.

Se tudo der certo, você verá: `Login Succeeded`

---

## Parte 3: O passo crucial - Etiquetando a imagem

Aqui mora o segredo. Uma imagem local precisa de um "endereço completo" antes de ser enviada ao Docker Hub.

### Entendendo o formato

```
docker tag <imagem-local> <usuario>/<repositorio>:<versao>
```

Onde:
- **usuario**: seu nome de usuário no Docker Hub
- **repositorio**: nome que você quer dar para a imagem
- **versao**: tag de versionamento (ex: 1.0.0, latest, dev)

### Na prática

Supondo que seu usuário seja `eduhkbr`:

```bash
docker tag solid-payment-api eduhkbr/payment-api:0.0.1
```

Agora se você rodar `docker images` novamente, verá duas entradas com o mesmo ID - não se preocupe, isso não duplica o espaço, apenas cria um novo "apelido" para a mesma imagem.

---

## Parte 4: Enviando para o Docker Hub

Com tudo preparado, hora de fazer o push:

```bash
docker push eduhkbr/payment-api:0.0.1
```

O Docker vai começar a enviar as camadas da imagem. Na primeira vez pode demorar alguns minutos, mas nas próximas será bem mais rápido - ele só envia o que mudou.

### Acompanhando o progresso

Você verá algo assim:
```
The push refers to repository [docker.io/eduhkbr/payment-api]
5f70bf18a086: Pushing [==============>        ] 32.5MB/112.3MB
d2c94e258dcb: Pushed
0.0.1: digest: sha256:0d4e8251c7261b586395a47c94863b7737af886787d377bc6de40efc9 size: 856
...
```

---

## Parte 5: Confirmando no Docker Hub

1. Acesse [hub.docker.com](https://hub.docker.com) e faça login
2. Vá para seus repositórios
3. Você deve ver `payment-api` listado
4. Clicando nele, verá a tag `0.0.1` que acabou de enviar

**Pronto!** Sua imagem está publicamente disponível.

---

## Como usar a imagem publicada

### Opção 1: Docker direto

Qualquer pessoa pode agora executar:

```bash
docker run -p 8080:8080 eduhkbr/payment-api:0.0.1
```

### Opção 2: Atualizando o docker-compose

No seu `docker-compose.yml`, substitua a seção de build pela imagem publicada:

```yaml
services:
  payment-api:
    # Antes (build local):
    # build:
    #   context: ./PaymentSolidApi
    #   dockerfile: Dockerfile
    
    # Depois (imagem do Hub):
    image: eduhkbr/payment-api:0.0.1
    
    container_name: payment_api_service
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    # resto das configurações...
```

Agora quando alguém rodar `docker-compose up`, o Docker vai automaticamente baixar a imagem do Hub ao invés de construí-la localmente.

---

## Dicas extras

### Versionamento semântico

Use tags significativas para suas versões:
- `0.0.1` - versões estáveis
- `latest` - sempre a mais recente
- `dev` - versão de desenvolvimento
- `0.0.1-hotfix` - correções urgentes

### Múltiplas tags

Você pode ter várias tags para a mesma imagem:

```bash
docker tag eduhkbr/payment-api:0.0.1 eduhkbr/payment-api:latest
docker push eduhkbr/payment-api:latest
```

### Automatizando com CI/CD

Em projetos reais, esse processo geralmente é automatizado. Ferramentas como GitHub Actions, GitLab CI ou Jenkins podem fazer o build e push automaticamente quando você faz um merge na branch principal.

---

## Troubleshooting comum

**Erro de permissão negada:**
- Certifique-se de estar logado: `docker login`

**Nome de repositório inválido:**
- Use apenas letras minúsculas, números e hífens
- Sem espaços ou caracteres especiais

**Push muito lento:**
- Normal na primeira vez
- Imagens menores são mais rápidas (use multi-stage builds)

**Imagem não encontrada após push:**
- Pode levar alguns segundos para aparecer no site
- Verifique se o push completou sem erros

---