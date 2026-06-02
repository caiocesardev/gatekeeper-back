# Arquitetura e Implementação — Gatekeeper Backend
Documento canônico da arquitetura do projeto.
## Visão geral
O Gatekeeper Backend segue uma estrutura inspirada em DDD, separando domínio, API, autenticação, segurança, mensageria e tratamento de erros.
### Tecnologias principais
- Kotlin 2.2+
- Spring Boot 4.0+
- Spring Data JPA
- Spring Security com JWT
- PostgreSQL 16
- MQTT com Eclipse Paho / Mosquitto
## Estrutura por camada
### `domain/`
Entidades e repositórios centrais do negócio.
#### Entidades
- `AppUser`: autenticação e perfis (`ADMIN`, `MANAGER`, `CARDHOLDER`)
- `AccessPoint`: ponto de entrada físico/ESP32 com identificador MQTT
- `RfidCredential`: credencial RFID vinculada a um cardholder
- `AccessLog`: histórico de acesso, leitura apenas
- `OneTimePassword`: código OTP usado no fluxo de ativação
#### Repositórios
- `AppUserRepository`: busca por e-mail e role
- `AccessPointRepository`: busca por identificador MQTT
- `RfidCredentialRepository`: validação de hexcode único
- `AccessLogRepository`: dados históricos
- `OneTimePasswordRepository`: suporte ao fluxo OTP
### `auth/`
Fluxo de login, setup de senha e OTP.
#### Endpoints
- `POST /api/auth/login`
- `POST /api/auth/setup-password`
- `POST /api/auth/validate-otp`
#### Componentes
- `AuthService`: login, setup de senha e validação OTP
- `OtpService`: geração e validação de OTP
- `JwtService`: geração de tokens
- `JwtValidator`: validação e extração de claims
- `JwtAuthenticationFilter`: popula o `SecurityContext`
### `api/admin/`
Responsável por perfis administrativos.
#### Endpoints
- `POST /api/admin/managers`
- `GET /api/admin/managers`
#### Componentes
- `AdminUserService`
- `AdminUserController`
### `api/manager/`
Responsável por operação de pontos de acesso e credenciais.
#### Endpoints
- `POST /api/manager/access-points`
- `GET /api/manager/access-points`
- `POST /api/manager/rfid-credentials`
- `GET /api/manager/access-logs`
#### Componentes
- `ManagerService`
- `ManagerController`
### `api/cardholder/`
Consulta dos próprios acessos do usuário autenticado.
#### Endpoints
- `GET /api/cardholder/access-logs`
#### Componentes
- `CardholderService`
- `CardholderController`
### `core/security/`
Segurança da aplicação.
- `SecurityConfig`: stateless, CSRF desabilitado, filtro JWT
- `JwtAuthenticationFilter`: lê `Authorization: Bearer <token>`
- `JwtAuthenticationProvider`: base para autenticação por token
- `JwtProperties`: chave e expiração do JWT
- `PasswordEncoderConfig`: `BCryptPasswordEncoder`
- `RateLimiter`: proteção simples contra brute force no login
### `core/exception/`
Tratamento centralizado de erros.
- `GlobalExceptionHandler`: converte exceções em respostas padronizadas
- `ErrorResponse`: payload estruturado para erros
### `core/config/`
Configurações de inicialização.
- `AdminSeeder`: cria admin padrão na primeira execução
- `MqttConfig`: conexão com o broker MQTT
### `messaging/subscriber/`
Integração com eventos MQTT.
- `AccessEventSubscriber`: recebe eventos de acesso e persiste `AccessLog`
## Regras de negócio principais
- `AppUser.email` deve ser único.
- `AccessPoint.mqttIdentifier` deve ser único.
- `RfidCredential.hexCode` deve ser único.
- `RfidCredential.appUser` deve apontar para um `CARDHOLDER`.
- `AppUser.isActive = false` indica usuário ainda não ativado.
- `deletedAt != null` representa exclusão lógica.
## Fluxos implementados
### Onboarding de usuário
1. Admin cria um manager.
2. OTP é gerado para ativação.
3. O usuário chama `POST /api/auth/setup-password` ou `POST /api/auth/validate-otp`.
4. A conta é ativada e um JWT é retornado.
### Login
1. Usuário envia e-mail e senha.
2. O sistema valida credenciais e aplica rate limiting.
3. Um JWT é retornado com a role do usuário.
### Operação do manager
1. Cria access points.
2. Vincula RFID a cardholders.
3. Consulta logs de acesso.
### Consulta do cardholder
1. Usuário autenticado acessa seus próprios logs.
2. O `SecurityContext` define a identidade do usuário.
## Próximas evoluções sugeridas
- Envio real de OTP por e-mail
- Auditoria de ações administrativas
- Swagger/OpenAPI
- Testes automatizados
- Soft delete com endpoints de restauração
- Webhooks e relatórios
## Como executar
### Pré-requisitos
- Java 21+
- Docker e Docker Compose
### Start local
```bash
docker compose up -d
./gradlew bootRun
```
### Documentação relacionada
- `README.md`: guia de execução do projeto
