# Implementações Realizadas — Gatekeeper Backend

## 📋 Resumo das Funcionalidades Implementadas

Este documento detalha as 5 principais implementações realizadas após a arquitetura base:

---

## 1. ✅ OTP (One-Time Password)

### Modelo
- **Entity**: `OneTimePassword` – armazena código, expiração, status de uso
- **Campos**: `code`, `expiresAt`, `isUsed`, `usedAt`
- **Validações**: código único e não utilizado

### Repositório
- **OneTimePasswordRepository**: queries para buscar OTP não utilizado

### Serviço
- **OtpService**:
  - `generateAndSendOtp(email)`: gera 6 dígitos, valida anteriores, scheduler de 10 min
  - `validateOtp(code)`: valida e marca como utilizado
  - TODO: integração com serviço de e-mail

### Endpoints
- `POST /api/auth/validate-otp`: valida código OTP e define senha
  - Request: `{ email, code, password }`
  - Response: `{ message, token }`

### Fluxo
```
1. Admin cria Manager → Manager fica inativo
2. Email com OTP (simula para dev)
3. Manager chama /auth/validate-otp
4. Ativa conta e retorna JWT
```

---

## 2. ✅ Paginação (Page & Pageable)

### Alterações
- **AppUserRepository**: `findAllByRole(Role)` agora usa `Page<AppUser>`
- **ManagerService**: 
  - `listAccessPoints(pageable)` → `Page<AccessPointResponse>`
  - `listAccessLogs(pageable)` → `Page<AccessLogResponse>`
- **AdminUserService**: 
  - `listManagers(pageable)` → `Page<AppUserResponse>`

### Controllers Atualizados
- `/api/admin/managers?page=0&size=10`
- `/api/manager/access-points?page=0&size=20`
- `/api/manager/access-logs?page=0&size=20`

### Comportamento
```bash
# Exemplo de requisição paginada
GET /api/manager/access-points?page=0&size=10&sort=id,desc
Authorization: Bearer <JWT>

# Response inclui:
{
  "content": [...],
  "pageable": { "pageNumber": 0, "pageSize": 10 },
  "totalElements": 45,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## 3. ✅ Soft Delete (deletedAt)

### Alterações Estruturais
Todas as entidades principais agora possuem:
```kotlin
@Column
var deletedAt: LocalDateTime? = null
```

**Entidades afetadas**:
- AppUser
- AccessPoint
- RfidCredential

### Queries Customizadas
Todos os repositórios agora filtram por `deletedAt IS NULL`:

```kotlin
@Query("SELECT u FROM AppUser u WHERE u.email = ?1 AND u.deletedAt IS NULL")
fun findByEmail(email: String): AppUser?

@Query("SELECT a FROM AccessPoint a WHERE a.deletedAt IS NULL")
fun findAllActive(): List<AccessPoint>
```

### Comportamento
- Exclusões lógicas preservam histórico
- Consultas automáticas ignoram deletados
- Perfil de Admin pode restaurar se necessário (TODO)

---

## 4. ✅ Rate Limiting

### Proteção
- Implementado no endpoint `/api/auth/login`
- **Limite**: 5 tentativas por 15 minutos (por email)

### Serviço
**RateLimiter**:
```kotlin
fun isAllowed(key: String): Boolean
  // Verifica janela de 15 min, máx 5 tentativas
  
fun reset(key: String)
  // Reseta contador (útil após sucesso)
```

### Integração no AuthService
```kotlin
fun login(request: LoginRequest): AuthResponse {
    if (!rateLimiter.isAllowed("login:${request.email}")) {
        throw ResponseStatusException(
            HttpStatus.TOO_MANY_REQUESTS, 
            "Muitas tentativas de login..."
        )
    }
    // ... resto do login
}
```

### Resposta HTTP
```
Status: 429 (Too Many Requests)
{
  "status": 429,
  "title": "Too Many Requests",
  "message": "Muitas tentativas de login. Tente novamente em 15 minutos."
}
```

---

## 5. ✅ MQTT Subscriber (AccessLog)

### Subscriber
**AccessEventSubscriber** – inscrito no tópico `gatekeeper/access/+`

### Formato de Mensagem Esperado
```json
{
  "tagRead": "ABC123",
  "isGranted": true,
  "denialReason": null
}
```

### Comportamento
1. Subscreve ao inicializar a aplicação
2. Ouve eventos de pontos de acesso (ESP32)
3. Cria `AccessLog` automaticamente
4. Extrai mqtt_identifier do tópico
5. Associa ao AccessPoint correto

### Exemplo de Publish
```bash
# De um ESP32 ou cliente MQTT
mosquitto_pub -h localhost -t "gatekeeper/access/GATE_01" \
  -m '{"tagRead":"ABC123","isGranted":true,"denialReason":null}'

# Resultado: AccessLog criado no banco com timestamp = agora
```

### Logs
```
📝 AccessLog criado: tag=ABC123, ponto=GATE_01, concedido=true
```

---

## 📊 Resumo de Arquivos Criados/Modificados

### ✨ Novos Arquivos
```
domain/
  model/
    └─ OneTimePassword.kt (JPA Entity)
  repository/
    └─ OneTimePasswordRepository.kt

auth/
  service/
    └─ OtpService.kt
  dto/
    └─ ValidateOtpRequest.kt

core/
  security/
    └─ RateLimiter.kt

messaging/
  subscriber/
    └─ AccessEventSubscriber.kt
```

### 🔄 Arquivos Modificados
```
domain/
  model/
    ├─ AppUser.kt (+ deletedAt)
    ├─ AccessPoint.kt (+ deletedAt)
    └─ RfidCredential.kt (+ deletedAt)
  repository/
    ├─ AppUserRepository.kt (+ queries soft-delete + findByEmail)
    ├─ AccessPointRepository.kt (+ queries soft-delete + findAllActive)
    └─ RfidCredentialRepository.kt (+ queries soft-delete)

auth/
  service/
    ├─ AuthService.kt (+ RateLimiter, OtpService, validateOtpAndSetPassword)
  controller/
    └─ AuthController.kt (+ /validate-otp)

api/
  admin/
    service/
      └─ AdminUserService.kt (+ Pageable)
    controller/
      └─ AdminUserController.kt (+ Pageable)
  manager/
    service/
      └─ ManagerService.kt (+ Pageable, soft-delete filtering)
    controller/
      └─ ManagerController.kt (+ Pageable)
```

---

## 🧪 Exemplos de Uso

### 1. Criar Manager com OTP
```bash
# 1. Admin faz login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@gatekeeper.com","password":"admin123"}' \
  | jq -r '.token')

# 2. Admin cria manager
curl -X POST http://localhost:8080/api/admin/managers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"João Manager","email":"joao@example.com"}'

# 3. Backend imprime: 📧 OTP para joao@example.com: 123456 (expira em 10 minutos)

# 4. Manager valida OTP
NEW_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/validate-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@example.com","code":"123456","password":"senha123"}' \
  | jq -r '.token')

# Agora NEW_TOKEN é válido para usar como Manager
```

### 2. Rate Limiting em Login
```bash
# Deve falhar na 6ª tentativa (5 permitidas em 15 min)
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"joao@example.com","password":"errada"}'
  echo "\nTentativa $i"
done

# Resposta da 6ª: 429 Too Many Requests
```

### 3. Paginação em Listagens
```bash
# Listar managers com paginação
curl -X GET "http://localhost:8080/api/admin/managers?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"

# Listar AccessPoints (página 1, 10 itens)
curl -X GET "http://localhost:8080/api/manager/access-points?page=1&size=10" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

### 4. Soft Delete (apenas via DB manual por enquanto)
```sql
-- Deletar manager (soft)
UPDATE app_users SET deleted_at = NOW() 
WHERE email = 'joao@example.com';

-- Verificar que não aparece mais
SELECT * FROM app_users WHERE deleted_at IS NULL;
```

### 5. MQTT Access Event
```bash
# Simular ESP32 publicando acesso permitido
mosquitto_pub -h localhost -t "gatekeeper/access/GATE_01" \
  -m '{"tagRead":"XYZ789","isGranted":true,"denialReason":null}'

# Verificar que AccessLog foi criado
curl -X GET "http://localhost:8080/api/manager/access-logs?page=0&size=10" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

---

## 📝 Próximas Implementações Sugeridas

1. **Envio Real de OTP**: integração com `spring-boot-starter-mail` (Gmail, SendGrid, etc)
2. **Auditoria**: registrar TODAS as ações (criação, update, delete, login) em tabela separada
3. **Restauração de Soft Delete**: endpoint `PATCH /api/admin/users/{id}/restore`
4. **Roles Dinâmicas**: permitir criar papéis novos além dos 3 padrões
5. **Cache de Permissões**: Redis para perfs de autorização repetidas
6. **Webhooks**: notificar sistemas externos de eventos importantes
7. **Relatórios**: endpoints de BI para acessos, negativas, etc
8. **2FA**: autenticação de dois fatores com app (TOTP)

---

## 🚀 Ativação no Startup

Todos os componentes estão automáticos:

- ✅ **OtpService**: carregado no contexto Spring
- ✅ **RateLimiter**: singleton disponível para AuthService
- ✅ **Soft Delete**: executado nas queries via `@Query` na inicialização
- ✅ **Paginação**: suporte nativo do Spring Data JPA
- ✅ **MQTT Subscriber**: ativa na inicialização via `@PostConstruct`

---

## ✅ Checklist de Validação

- [x] OTP gerado e validado
- [x] Rate limiting ativo em login (5 tentativas/15 min)
- [x] Soft delete em todas as entidades principais
- [x] Paginação em todos os listadores
- [x] MQTT subscriber criando AccessLog automaticamente
- [x] Sem erros de compilação
- [x] DTOs padronizados
- [x] Transações configuradas (`@Transactional`)
- [x] Segurança mantem JWT flow intacto

---

**Data**: 2026-06-01  
**Status**: ✅ Pronto para Produção (com TODO de e-mail)  
**Versão**: 0.0.2-FEATURES

