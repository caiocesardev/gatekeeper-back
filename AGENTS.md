# 🤖 AI Agents Instructions - Gatekeeper Backend

> **Context for AI Agents (GitHub Copilot, Gemini Code Assist, etc.):** > When interacting with this workspace, answering questions, or generating code, you MUST adhere strictly to the rules, tech stack, and architecture defined in this document.

---

## 1. 🛠️ Tech Stack & Environment
- **Language:** Kotlin (version 2.3+)
- **Framework:** Spring Boot 4.0+
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** PostgreSQL 18+
- **Messaging/IoT:** Eclipse Paho MQTT (Mosquitto)
- **Security:** Spring Security + JWT (`com.auth0:java-jwt:4.4.0`)
- **Build Tool:** Gradle (Kotlin DSL)

### Repository-specific versions (observed)
- Kotlin plugin: 2.3.21 (see `build.gradle.kts`)
- Spring Boot: 4.0.6 (see `build.gradle.kts`)
- Java toolchain: Java 21 (configured in `build.gradle.kts`)
- MQTT client library: `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5` (dependency)
- JWT library: `com.auth0:java-jwt:4.4.0` (dependency)
- Note: `README.md` currently lists Kotlin 2.2.x — the actual build uses Kotlin 2.3.21. Prefer the versions above for generator decisions.

---

## 2. 🗣️ Language & Naming Conventions
- **CRITICAL RULE 1 (Code):** ALL source code—including package names, class names, variables, methods, properties, and inline code comments—MUST be written in **English**.
- **CRITICAL RULE 2 (Text):** ALL user-facing texts, exception messages, email bodies, HTTP response messages, and console log descriptions MUST be written in **Brazilian Portuguese (pt-BR)**.
    - *Example:* `fun sendWelcomeEmail(user: AppUser) { println("Bem-vindo ao Gatekeeper!") }`

---

## 3. 🏗️ Architecture & Package Structure
This project follows a simplified Domain-Driven Design (DDD) approach. You must respect package boundaries:
- `core/`: Global configurations (`SecurityConfig`, `MqttConfig`), global exception handlers, and security filters (`JwtAuthenticationFilter`, `RateLimiter`).
- `domain/`: Centralized business rules containing ONLY `@Entity` models and Spring Data `Repositories`.
- `messaging/`: Isolation for IoT communication. Contains MQTT subscribers (`AccessEventSubscriber`) and publishers.
- `auth/`: Services and Controllers handling Login, OTP generation, token validation, and Emails.
- `api/`: REST Controllers, Services, and DTOs. It is strictly divided by RBAC profiles:
    - `api/admin/`: Accessible only by Sysadmins.
    - `api/manager/`: Accessible only by Building Managers.
    - `api/cardholder/`: Accessible only by End Users.

---

## 4. 🔒 Security & RBAC (Role-Based Access Control)
- **Authentication:** All secure routes require a valid JWT passed in the `Authorization: Bearer <token>` header.
- **Roles:** Handled via the `Role` Enum: `ADMIN`, `MANAGER`, `CARDHOLDER`.
- **Authorization:** `SecurityConfig` restricts endpoints by role (e.g., `/api/admin/**` requires `hasRole("ADMIN")`).
- **Rate Limiting:** Auth routes (like `/api/auth/login`) are protected by a custom `RateLimiter` (e.g., max 5 attempts / 15 minutes).

---

## 5. 🗄️ Domain Entities & Business Rules
When generating JPA queries or new services, use the following entity map:

1. **`AppUser`**: Single table for all users. Fields: `id`, `fullName`, `email` (unique), `password`, `role`, `isActive`, `deletedAt`.
2. **`AccessPoint`**: The physical ESP32 hardware. Fields: `id`, `mqttIdentifier` (e.g., "GATE_01"), `locationDescription`, `isUnderMaintenance`, `deletedAt`.
3. **`RfidCredential`**: The physical RFID tag. Fields: `id`, `hexCode` (unique), `appUser` (FK), `isBlocked`, `deletedAt`.
4. **`AccessLog`**: Read-only history. Fields: `id`, `tagRead`, `accessPoint` (FK), `timestamp`, `isGranted`, `denialReason`.
5. **`OneTimePassword`**: Temp codes. Fields: `id`, `appUser` (FK), `code`, `expiresAt`, `isUsed`, `usedAt`.

---

## 6. 🧑‍💻 Coding Standards & Generation Rules
When writing Kotlin code for this project, you MUST apply these standards:
1. **DTOs Mandatory:** ALWAYS use Data Transfer Objects (DTOs) for Requests and Responses in the `api/` and `auth/` layers. NEVER expose `@Entity` classes directly in Controllers.
2. **Soft Deletes:** Entities have a `deletedAt` field. Use Soft Delete logic (e.g., `UPDATE ... SET deletedAt = NOW()`) instead of hard `DELETE` statements. Repositories must filter out records where `deletedAt IS NOT NULL`.
3. **Pagination:** `GET` endpoints returning lists must always return a Spring `Page<T>` and accept `Pageable` parameters.
4. **Constructor Injection:** Use Kotlin primary constructors for dependency injection (avoid `@Autowired` on fields).