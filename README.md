# 🛡️ Gatekeeper: Sistema Híbrido de Controle de Acesso IoT

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-blue.svg?logo=kotlin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18+-blue.svg?logo=postgresql)
![MQTT](https://img.shields.io/badge/MQTT-Mosquitto-orange.svg?logo=eclipse-mosquitto)
![Security](https://img.shields.io/badge/Security-JWT%20%7C%20OTP-blue.svg?logo=jsonwebtokens)
![Tests](https://img.shields.io/badge/Tests-JUnit%205%20%7C%20MockK-red.svg?logo=junit5)

**Gatekeeper** é uma solução de nível empresarial (Enterprise-grade) projetada para orquestrar o controle de acesso físico através de dispositivos de borda (IoT). Nascido com resiliência em mente, ele suporta operação mesmo em cenários de degradação de conectividade.

---

## 🏗️ 1. Visão Geral e Arquitetura Híbrida

O projeto adota uma arquitetura de sistema distribuído com **topologia bifurcada**, oferecendo duas interfaces de comunicação altamente especializadas para diferentes atores do ecossistema:

*   🌐 **Interface HTTP/REST (Síncrona):**
    Destinada aos painéis de gestão, administradores de condomínios e usuários finais. Provê endpoints protegidos por *Role-Based Access Control (RBAC)* e **Spring Security com JWT**. Todo o tráfego gerencial, relatórios, gestão de perfis e fluxos de autenticação (incluindo redefinição de senha com OTP) ocorrem através dessa interface clássica e robusta.
*   📡 **Interface MQTT (Assíncrona Orientada a Eventos):**
    Uma via exclusiva para a comunicação de telemetria e comandos com os hardwares de borda (pontos de acesso, leitores RFID baseados em ESP32). Utiliza o protocolo leve **MQTT (via Eclipse Mosquitto)** para garantir *low-latency* nas validações de tags RFID, reduzir o *overhead* de cabeçalhos HTTP e operar por *publish/subscribe* em tópicos altamente isolados de segurança.

---

## ✨ 2. Funcionalidades Principais

*   **Gestão de Acesso por Perfil (RBAC):** Divisão clara de responsabilidades entre Administradores, Gestores e Portadores (Cardholders).
*   **Autenticação Segura:** Login com JWT e fluxo completo de redefinição de senha utilizando OTP (One-Time Password).
*   **Gestão de Portadores (Cardholders):** CRUD completo para usuários finais, com ativação/inativação e associação de credenciais.
*   **Inteligência de Borda (Edge Computing):** Sincronização de cache com dispositivos IoT para garantir operação offline.
*   **Auditoria Completa:** Logs detalhados de todos os eventos de acesso.

---

## 🧠 3. Inteligência de Borda (Edge Computing) e Resiliência

Uma infraestrutura crítica de segurança física não pode depender de uma nuvem com 100% de disponibilidade. O Gatekeeper delega inteligência aos dispositivos através do fluxo de **Resiliência e Modo Offline**.

**Mecanismo de Sincronização e Fallback:**
*   **Cache Proativo no Hardware:**
    O Back-end, por meio do componente `CacheSyncService`, publica via MQTT uma lista atualizada de *Hashes de Credenciais Válidas* sempre que há uma alteração nos painéis gerenciais (ex: inativação de um usuário).
*   **Modo Offline Seguro:**
    Se o Ponto de Acesso (hardware) perder conexão com a rede Wi-Fi ou com o Broker MQTT, ele continua liberando catracas/portas validando as leituras de RFID localmente contra as chaves previamente salvas em sua memória Flash não volátil.
*   **Reconciliação Assíncrona:**
    Durante o *Modo Offline*, todo e qualquer log de acesso (concedido ou negado) é enfileirado localmente na borda. Quando a conexão é restabelecida, o dispositivo descarrega a fila reprimida (*burst*) através do MQTT. Os *Subscribers* no back-end consomem esses eventos retroativos e reconciliam a base de auditoria (`AccessLog`).

---

## 🛠️ 4. Stack Tecnológica

Todo o core do produto foi modernizado para as mais recentes plataformas do mercado de desenvolvimento corporativo:

| Tecnologia | Descrição / Uso |
| :--- | :--- |
| **Kotlin (2.3.21)** | Linguagem principal adotando concisão, *null-safety* e classes *data/value*. |
| **Spring Boot (4.0.6)** | Framework base, provendo Injeção de Dependência, Data JPA/Hibernate e Web MVC. |
| **PostgreSQL (18+)** | RDBMS para persistência de dados críticos, domínios e logs com alta confiabilidade. |
| **Eclipse Mosquitto** | Message Broker *open-source* padrão da indústria para IoT (MQTT). |
| **Spring Security & JWT** | Autenticação *stateless* e controle refinado de autorização baseada em Roles (ADMIN, MANAGER, CARDHOLDER). |
| **ArduinoJson** | No firmware da borda (C++), os pacotes trafegados via MQTT utilizam esse parser robusto. |

---

## 📖 5. Documentação Doc-as-Code

O mapeamento da plataforma, desde a sua infraestrutura até o contrato com os clientes, vive e evolui no mesmo ciclo de vida que a base de código (*Doc-as-Code*).

*   **API REST (Swagger / OpenAPI 3):**
    Contratos estritos, *schemas* de requisição/resposta, DTOs e requisitos de segurança. Acessível em tempo de desenvolvimento através de:
    ➡️ `http://localhost:8080/swagger-ui.html`
*   **Mensageria IoT (AsyncAPI):**
    Documentação mapeando os canais, tópicos de subscrição (`gatekeeper/access/events`), padrões de payload de telemetria (C2D) e Comandos de Borda (D2C). Acessível em:
    ➡️ `http://localhost:8080/asyncapi-ui.html`

---

## 🧪 6. Engenharia de Qualidade

Código de segurança exige garantias de execução estritas. A cultura de qualidade do projeto é orientada à Testes Automatizados da pirâmide base:

*   **Testes Unitários:** Operados via **JUnit 5**, verificando intensamente a camada de Domínio, `Services` e fluxos de `Security`.
*   **MockK Nativo:** Empregamos `MockK` pela aderência orgânica ao ecossistema Kotlin. Utilizamos extensivamente a injeção nativa de dependências via **Construtor Primário** para provisionar Mocks, banindo injeções de campo por *Reflection* (`@Autowired`), o que previne erros mascarados e garante testes mais velozes.
*   **Análise de Cobertura:** Com **JaCoCo**, emitimos relatórios em pipeline que validam a cobertura mínima, prevenindo a introdução de rotinas com pontas soltas.

---

## 🚀 7. Guia de Execução

Seja para desenvolvimento iterativo ou deploy local, a inicialização da plataforma é direta e exige zero configuração manual no sistema operacional host.

**Passo 1: Subir Infraestrutura de Apoio (PostgreSQL, Broker MQTT)**
Na raiz do projeto, acione o manifesto Docker:
```bash
docker-compose up -d
```

**Passo 2: Iniciar o Application Server**
Utilizando o Gradle Wrapper para baixar dependências e compilar a aplicação na JVM 21+:
```bash
./gradlew bootRun
```
