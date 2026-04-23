# Neversion — Arquitectura del Sistema

> Consolida la arquitectura técnica del sistema, los diagramas de componentes y las metodologías de desarrollo.

---

## 1. Visión de Alto Nivel

El sistema sigue una arquitectura **desacoplada API-driven**. El frontend actúa como cliente puro consumiendo una API RESTful segura provista por el backend.

```mermaid
flowchart TB
    Admin(("Administrador"))
    Customer(("Cliente / Invitado"))

    subgraph "Frontend Layer"
        Angular["Angular 17 Admin Panel\n(Standalone, Signals, Bootstrap 5)"]
    end

    subgraph "Backend Layer"
        SpringBoot["Spring Boot 3 REST API\n(Hexagonal Architecture, DDD)"]
    end

    subgraph "External & Cloud Services"
        SupabaseAuth{" Supabase Auth\n(Identity Provider)"}
        PostgreSQL[(" PostgreSQL\n(Relational Database)")]
        S3[(" AWS S3\n(Receipt Image Storage)")]
    end

    Admin -->|Manages inventory & orders| Angular
    Customer -->|Uploads payment receipt| SpringBoot

    Angular -->|Authenticates via supabase-js| SupabaseAuth
    Angular <-->|Consumes REST API JWT Bearer| SpringBoot

    SpringBoot <-->|Reads-Writes Data| PostgreSQL
    SpringBoot -->|Validates JWT Signature| SupabaseAuth
    SpringBoot -->|Uploads-Retrieves URLs| S3
```

---

## 2. Backend — Arquitectura Hexagonal (Ports & Adapters) y DDD

El backend en Spring Boot sigue estrictamente **Hexagonal Architecture** combinada con **Domain-Driven Design (DDD)**. Esto aísla la lógica de negocio del framework, la base de datos y la UI.

```mermaid
flowchart LR
    subgraph "Infrastructure (Driving Adapters)"
        REST["REST Controllers\n(@RestController)"]
    end

    subgraph "Core Business (Hexagon)"
        InPort("Inbound Ports\n(Use Case Interfaces)")
        AppService["Application Services\n(Use Case Implementations)"]
        Domain{"Domain Models\n(Aggregates, Entities)"}
        OutPort("Outbound Ports\n(Repository Interfaces)")

        InPort --> AppService
        AppService --> Domain
        AppService --> OutPort
    end

    subgraph "Infrastructure (Driven Adapters)"
        JPA["JPA Repositories\n(PostgreSQL)"]
        S3Adapter["S3 Storage Service"]
    end

    REST -->|Triggers Use Case| InPort
    OutPort -->|Implemented by| JPA
    OutPort -->|Implemented by| S3Adapter
```

### Capas del Backend

| Capa | Contenido | Reglas |
|---|---|---|
| **Domain** | Aggregates, Entities, Value Objects | Pure Java, cero dependencias de framework |
| **Application** | Use Cases (Inbound Ports + Application Services) | Orquesta lógica de negocio |
| **Infrastructure** | REST Controllers, JPA Repositories, S3 Adapter | Adapters al mundo externo |

**Convenciones de código:**

- Naming: `com.neversion.api.<feature>.<layer>`
- Interfaces: `<Name>Port`, `<Name>UseCase`
- Inyección: solo via constructor (no `@Autowired` en campos)
- DTOs: Java Records
- Mappers: `RequestMapper`, `ResponseMapper`, `EntityMapper` (no MapStruct)
- Soft Delete: `@SQLDelete` + `@SQLRestriction`

---

## 3. Frontend — Angular 17

```
panel/ (Angular 17, pnpm)
├── Standalone Components     ← No NgModule
├── Signals                   ← signal(), computed(), effect() para state
├── Reactive Forms            ← Para formularios
├── Smart / Dumb Components   ← Pages (Smart) + UI elements (Dumb)
└── Bootstrap 5 + SCSS/OKLCH  ← Estilos
```

- **Smart Components (Pages):** Inyectan servicios, manejan estado.
- **Dumb Components (UI Elements):** Reciben datos via `@Input()`, emiten eventos via `@Output()`.
- **Paginación (Sprint 1):** Backend retorna listas completas. El frontend almacena en Signal y deriva vistas paginadas con `computed()`.

---

## 4. Flujo de Autenticación (OAuth2 / JWT)

Supabase Auth actúa como Identity Provider. Spring Boot actúa como OAuth2 Resource Server.

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Angular as Angular 17 (Frontend)
    participant Supabase as Supabase Auth
    participant SpringBoot as Spring Boot (Resource Server)

    Admin->>Angular: Ingresa Email y Contraseña
    Angular->>Supabase: signInWithPassword()
    Supabase-->>Angular: Retorna Session (Access Token / JWT)

    note over Angular: HTTP Interceptor adjunta<br/>Authorization: Bearer <JWT>

    Angular->>SpringBoot: GET /api/v1/accounts

    note over SpringBoot: Spring Security intercepta request

    SpringBoot->>Supabase: Fetch JWKS (Public Keys) para verificar firma
    Supabase-->>SpringBoot: JWKS Data

    note over SpringBoot: Valida claims del token y roles (app_metadata)

    alt Token válido y rol ADMIN
        SpringBoot-->>Angular: 200 OK + JSON Data
        Angular-->>Admin: Renderiza Dashboard
    else Token expirado o rol inválido
        SpringBoot-->>Angular: 401 Unauthorized / 403 Forbidden
        Angular-->>Admin: Redirige al Login
    end
```

- **Roles:** Gestionados via JWT claims (`app_metadata`).
- **JWKS:** Spring Boot valida la firma descargando las public keys de Supabase.

---

## 5. Metodologías de Desarrollo

El proyecto adopta un enfoque híbrido:

### Modelo Incremental (Estrategia macro)
- El sistema se divide en incrementos funcionales. Cada uno entrega un producto operativo en producción.
- **Increment 1 / Sprint 1:** Capacidades operativas mínimas para lanzamiento manual.
- **Increment 2 / Sprint 2+:** Automatizaciones, flujos complejos, portal de autoservicio.

### Scrum (Motor de ejecución)
- Sprints de 1–2 semanas con eventos: Planning, Daily Standup, Review, Retrospective.
- **Product Owner:** Maximiza el valor del producto, gestiona el backlog.
- **Scrum Master:** Facilita el proceso, remueve impedimentos.
- **Developers:** Construyen el incremento.

> *"El Modelo Incremental define **QUÉ** entregar; Scrum define **CÓMO** el equipo colabora para construirlo."*

---

## Cuándo leer este archivo

- Antes de contribuir al backend (entender la estructura hexagonal)
- Antes de contribuir al frontend (entender componentes y signals)
- Para entender el flujo completo de autenticación
