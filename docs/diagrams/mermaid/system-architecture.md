# 2. System Architecture

This document outlines the high-level architecture, design patterns, and interaction flows between the different components of the system.

## 2.1. High-Level System Context

The system follows a decoupled, API-driven architecture. The frontend acts as a pure client consuming a secure RESTful API provided by the backend. External services are utilized for authentication and file storage.

```mermaid
flowchart TB
    Admin(("Administrator"))
    Customer(("Customer / Guest"))

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

    SpringBoot <-->|Reads-Writes Data | PostgreSQL
    SpringBoot -->|Validates JWT Signature | SupabaseAuth
    SpringBoot -->|Uploads-Retrieves URLs | S3
```

## 2.2. Backend: Hexagonal Architecture (Ports & Adapters) & DDD
The Spring Boot backend is strictly structured using Hexagonal Architecture (also known as Ports and Adapters) combined with Domain-Driven Design (DDD). This isolates the core business logic from framework-specific details, databases, and UI concerns.

- Domain Layer: Contains Aggregates, Entities, and Value Objects (e.g., Account, Profile, Subscription). Pure Java, zero framework dependencies.

- Application Layer (Use Cases): Orchestrates the business logic. It exposes Inbound Ports (Interfaces) implemented by Application Services.

- Infrastructure Layer (Adapters): Contains REST Controllers (Driving Adapters) and JPA Repositories/External API clients (Driven Adapters).

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

## 2.3. Frontend: Angular 17 Architecture
The frontend is built using modern Angular 17 paradigms:

- Standalone Components: Modules (NgModule) are not used. Components directly import what they need.

- Signals: Reactive state management is handled via Signals (signal, computed, effect) instead of heavy RxJS chains where possible, especially for client-side filtering and in-memory pagination (Sprint 1).

- Smart & Dumb Components: Pages (Smart) inject services and manage state, while UI elements (Dumb) receive data via @Input() and emit events via @Output().

## 2.4. Authentication Flow (OAuth2 / JWT)
Security is managed via Supabase Auth acting as the Identity Provider, while Spring Boot acts as an OAuth2 Resource Server.

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Angular as Angular 17 (Frontend)
    participant Supabase as Supabase Auth
    participant SpringBoot as Spring Boot (Resource Server)

    Admin->>Angular: Enters Email & Password
    Angular->>Supabase: signInWithPassword()
    Supabase-->>Angular: Returns Session (Access Token / JWT)
    
    note over Angular: HTTP Interceptor attaches<br/>Authorization: Bearer <JWT>
    
    Angular->>SpringBoot: GET /api/v1/accounts
    
    note over SpringBoot: Spring Security intercepts request
    
    SpringBoot->>Supabase: Fetch JWKS (Public Keys) to verify signature
    Supabase-->>SpringBoot: JWKS Data
    
    note over SpringBoot: Validates token claims & roles (app_metadata)
    
    alt Token is valid & User is ADMIN
        SpringBoot-->>Angular: 200 OK + JSON Data
        Angular-->>Admin: Renders Master Dashboard
    else Token expired or invalid role
        SpringBoot-->>Angular: 401 Unauthorized / 403 Forbidden
        Angular-->>Admin: Redirects to Login
    end
```

## 2.5. Data Pagination Strategy (Sprint 1)
For Sprint 1, the backend returns full unpaginated lists (e.g., all Subscriptions). The Angular frontend stores this complete list in a Signal and derives paginated/filtered views using computed() signals. This guarantees immediate UI updates. Server-side pagination will be introduced in Sprint 2 as data volume grows.