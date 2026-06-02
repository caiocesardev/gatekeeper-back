# GitHub Copilot Instructions for Gatekeeper Backend

## 1. Tech Stack
- Language: Kotlin (version 2.0+)
- Framework: Spring Boot 3.3.0
- Persistence: Spring Data JPA, Hibernate, PostgreSQL 16
- Integration: Eclipse Paho MQTT (Mosquitto)
- Security: Spring Security with JWT (com.auth0:java-jwt)

## 2. Language & Naming Conventions
- **CRITICAL:** All code (package names, class names, variables, methods, properties) MUST be written in English.
- **CRITICAL:** All user-facing texts, exception messages, email bodies, and log descriptions MUST be written in Brazilian Portuguese.
- Example: `fun sendEmail(user: AppUser)` -> "Bem-vindo ao Gatekeeper!"

## 3. Architectural Rules
- Follow a Domain-Driven Design (DDD) inspired structure.
- **core:** Global configs, exceptions, and security filters.
- **domain:** `@Entity` classes and Spring Data `Repositories`.
- **messaging:** MQTT publishers and subscribers.
- **auth:** Login, OTP generation, and Email sending.
- **api:** REST Controllers and Services, strictly divided into `admin`, `manager`, and `cardholder` sub-packages.
- ALWAYS use Data Transfer Objects (DTOs) for requests and responses in the `api` layer. Never expose Entities directly in Controllers.

## 4. Security & Role-Based Access Control (RBAC)
- Use a single `AppUser` entity for authentication.
- Differentiate permissions using an Enum `Role` (ADMIN, MANAGER, CARDHOLDER).