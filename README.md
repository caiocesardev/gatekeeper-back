# Gatekeeper-back

API backend do projeto Gatekeeper, construída com Kotlin + Spring Boot.

## Estado atual

O repositório já está configurado com base de projeto Spring Boot, Gradle Kotlin DSL e infraestrutura auxiliar (PostgreSQL e MQTT via Docker Compose).

## Stack

- Kotlin 2.3.x
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

