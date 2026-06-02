# Gatekeeper-back

API backend do projeto Gatekeeper, construída com Kotlin + Spring Boot.

## Estado atual

O repositório já está configurado com base de projeto Spring Boot, Gradle Kotlin DSL e infraestrutura auxiliar (PostgreSQL e MQTT via Docker Compose).

Consulte `docs/ARCHITECTURE.md` para a documentação consolidada da arquitetura e dos fluxos implementados.

## Stack

- Kotlin 2.2.x
- Spring Boot 4.0.x
- Spring Data JPA
- Spring Validation
- Spring Web MVC
- PostgreSQL
- MQTT (Eclipse Paho client + Mosquitto)
- Gradle Wrapper

## Pré-requisitos

- Java 21
- Docker e Docker Compose (opcional, para subir PostgreSQL/MQTT)

## Como executar

### 1) Subir dependências locais (PostgreSQL e MQTT)

Na raiz do projeto:

```bash
docker compose up -d database mqtt-broker
```

Serviços expostos por padrão:

- PostgreSQL: `localhost:5432`
- MQTT: `localhost:1883`
- MQTT WebSocket: `localhost:9001`

Credenciais padrão do PostgreSQL (definidas no `docker-compose.yml`):

- Banco: `gatekeeper_db`
- Usuário: `admin`
- Senha: `adminpassword`

### 2) Configurar variáveis de ambiente da aplicação

Antes de iniciar o backend, exporte as variáveis:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gatekeeper_db
export SPRING_DATASOURCE_USERNAME=admin
export SPRING_DATASOURCE_PASSWORD=adminpassword
export MQTT_BROKER_URL=tcp://localhost:1883
```

> Observação: atualmente o `application.properties` tem apenas `spring.application.name`. As conexões de banco/MQTT devem ser fornecidas por ambiente ou por um arquivo de propriedades adicional.

### 3) Rodar a aplicação com Gradle

```bash
./gradlew bootRun
```

A aplicação inicia na porta padrão do Spring Boot (`8080`), salvo configuração diferente.

## Executar com Docker (backend)

O repositório possui `Dockerfile` para gerar a imagem do backend.

### Build da imagem

```bash
docker build -t gatekeeper-back .
```

### Execução do container

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/gatekeeper_db \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=adminpassword \
  -e MQTT_BROKER_URL=tcp://host.docker.internal:1883 \
  gatekeeper-back
```

## Testes

Executar suíte de testes:

```bash
./gradlew test
```

## Estrutura do projeto

- `src/main/kotlin/com/webcrafters/gatekeeperback`: código Kotlin da aplicação
- `src/main/resources`: arquivos de configuração
- `mosquitto/config/mosquitto.conf`: configuração do broker MQTT
- `docker-compose.yml`: serviços auxiliares para desenvolvimento local
- `Dockerfile`: imagem da aplicação

## Próximos passos sugeridos

- Definir perfis (`application-dev.properties`, `application-prod.properties`)
- Versionar um `.env.example` com variáveis obrigatórias
- Incluir documentação dos endpoints (OpenAPI/Swagger)

## Implementações Recentes (v0.0.2)

Veja `IMPLEMENTATIONS.md` para detalhes sobre:

- ✅ **OTP (One-Time Password)**: Fluxo de onboarding com código OTP (6 dígitos, 10 min de validade)
- ✅ **Paginação**: Todos os listadores suportam `page` e `size`
- ✅ **Soft Delete**: Campos `deletedAt` preservam histórico
- ✅ **Rate Limiting**: Proteção contra brute force no login (5 tentativas/15 min)
- ✅ **MQTT Subscriber**: Cria `AccessLog` automaticamente ao receber eventos de ESP32

### Exemplos Rápidos

```bash
# OTP: Validate código recebido e ativa conta
curl -X POST http://localhost:8080/api/auth/validate-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","code":"123456","password":"senha123"}'

# Paginação: Listar com limite
curl "http://localhost:8080/api/manager/access-logs?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

