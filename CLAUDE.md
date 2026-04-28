# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a monorepo for the **Neversion System**, a platform for managing digital service subscriptions (streaming, gift cards, mobile top-ups). The project consists of:

- **`apps/api/`**: Spring Boot 3 REST API (Java 17) — Hexagonal Architecture + DDD
- **`apps/panel/`**: Angular 17 admin panel (TypeScript)
- **`apps/store/`**: Angular 16 customer-facing storefront (TypeScript)
- **`docs/`**: Architecture, business rules, ADRs, EPICs, bitácora

## High-Level Architecture

The system follows a decoupled, API-driven architecture with three layers:

```
┌─────────────────┐      ┌─────────────────┐
│  Admin Panel    │      │  Customer Site  │
│  (Angular 17)   │      │  (Angular 16)   │
└────────┬────────┘      └────────┬────────┘
         │                        │
         └────────┬───────────────┘
                  │ REST API (JWT)
         ┌────────▼────────┐
         │   Spring Boot   │
         │    Backend      │
         └────────┬────────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
┌───▼────┐   ┌───▼──────┐  ┌──▼────┐
│Supabase│   │PostgreSQL│  │AWS S3 │
│  Auth  │   │    DB    │  │Storage│
└────────┘   └──────────┘  └───────┘
```

### Architecture Style

- **Backend**: Hexagonal Architecture (Ports & Adapters) + Domain-Driven Design (DDD)
  - Domain layer: Pure business logic, zero framework dependencies
  - Application layer: Use case orchestration
  - Infrastructure layer: REST controllers, JPA repositories, external adapters

- **Frontend**: Feature-based architecture with standalone components (Angular 17) and module-based (Angular 16)
  - Smart/Dumb component pattern
  - Signals for reactive state management (panel only)
  - Supabase Auth for authentication

## Development Setup

### Prerequisites

- **Java 17**
- **Maven 3.8+**
- **Node.js 18+** (use pnpm for panel, npm for store)
- **PostgreSQL 16** (or Supabase)

### Environment Configuration

The `api/` requires environment variables. Copy `api/.env.example` to `api/.env` and configure:
- Database connection (PostgreSQL)
- Supabase Auth JWT settings
- AWS S3 credentials (for receipt uploads)

## API (Backend) Commands

### Working Directory
All backend commands must be run from the `apps/api/` directory.

```bash
cd apps/api

# Start development server
./mvnw spring-boot:run

# Build without tests
./mvnw clean package -DskipTests

# Build and run all tests
./mvnw clean install

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=CreateAccountServiceUT

# Run a single test method
./mvnw test -Dtest=CreateAccountServiceUT#create_shouldReturnSavedAccount

# Compile only
./mvnw compile

# Full verification including integration tests
./mvnw verify
```

### Backend Code Structure

Strict hexagonal architecture per feature:

```
api/src/main/java/com/neversion/api/<feature>/
├── domain/
│   ├── model/              # Pure domain entities, value objects, enums
│   ├── port/out/           # Repository interfaces (outbound ports)
│   └── service/            # Domain services with business logic
├── application/
│   ├── port/in/            # Use case interfaces (inbound ports)
│   └── service/            # Use case implementations (orchestration only)
└── infrastructure/
    ├── adapters/in/rest/   # REST controllers, DTOs, request/response mappers
    ├── adapters/out/       # JPA entities, Spring Data repositories, persistence mappers
    └── config/             # Per-module SecurityConfig (HttpSecurityCustomizer)
```

**Active modules:** `user`, `vendor`, `client`, `service`, `account`, `profile`, `reservation`, `order`, `subscription`

**Agent protocol:** See `docs/agents/CLAUDE.md` for the backend agent's operational protocol (plan → code → test → log).

### Backend Guidelines

- **Dependency Injection**: Constructor injection only (no `@Autowired` on fields)
- **DTOs**: Use Java Records for immutability or `@Builder` (Lombok) for mutability
- **Validation**: Jakarta Validation (`@NotNull`, `@NotBlank`, `@Valid`)
- **Mappers**: Manual mappers (no MapStruct) - separate for DTOs and persistence
- **REST API**: All endpoints prefixed with `/api/v1/`
- **Error Handling**: Use `@ControllerAdvice` for global exception handling
- **Testing**: JUnit 5 + Mockito + AssertJ, Testcontainers for integration tests
- **Test Naming**: `<method>_<scenario>_<expected>` with `@DisplayName`
- **Database**: Use Flyway migrations in `api/src/main/resources/db/migration/`

### Database Migrations

Flyway migrations are versioned SQL files in `api/src/main/resources/db/migration/`:
- Format: `V<number>__<description>.sql` (e.g., `V10__reservation_orders_details.sql`)
- Migrations run automatically on application startup
- Never modify existing migrations; create new ones for changes

## Panel (Admin Frontend) Commands

### Working Directory
All panel commands must be run from the `apps/panel/` directory.

```bash
cd apps/panel

# Install dependencies (use pnpm)
pnpm install

# Start development server (http://localhost:4200)
pnpm start
# or: ng serve

# Build for production
pnpm run build
# or: ng build

# Build in watch mode
pnpm run watch

# Run all tests
pnpm test
# or: ng test

# Run a single test file (requires karma.conf.js)
ng test --include="**/auth.service.spec.ts"
```

### Panel Code Structure

Feature-based architecture with standalone components (Angular 17):

```
panel/src/app/
├── core/                   # Shared/singleton services and guards
│   ├── guards/             # Route guards (auth.guard.ts, guest.guard.ts)
│   └── services/           # Core services (auth.service.ts, supabase.service.ts)
├── features/               # Feature modules (lazy-loaded) — to be created per feature
├── shared/                 # Shared UI components
├── app.component.ts        # Root component
├── app.config.ts           # Application providers
└── app.routes.ts           # Route definitions with lazy loading
```

### Panel Guidelines

- **Angular 17**: Standalone components (no NgModules)
- **State Management**: Signals (`signal()`, `computed()`, `effect()`) for reactive state
- **Forms**: Reactive Forms only (`FormBuilder`, `FormGroup`, `Validators`)
- **Control Flow**: New Angular 17 syntax (`@if`, `@for`, `@defer`) over `*ngIf`, `*ngFor`
- **Styling**: Bootstrap 5 utilities first, custom SCSS only when necessary
- **UI Design**: Sober and modern (cards with `shadow-sm`, `rounded` or `rounded-3`)
- **Guards**: Functional guards (`CanActivateFn`) instead of class-based
- **Lazy Loading**: Use `loadComponent` in routes
- **Authentication**: Supabase JWT via `@supabase/supabase-js`

## Store (Customer Site) Commands

### Working Directory
All store commands must be run from the `apps/store/` directory.

```bash
cd apps/store

# Install dependencies (use npm)
npm install

# Start development server (http://localhost:4200)
npm start
# or: ng serve

# Build for production
npm run build
# or: ng build

# Build in watch mode
npm run watch

# Run tests
npm test
# or: ng test

# SSR (Server-Side Rendering) commands
npm run dev:ssr          # Development SSR server
npm run build:ssr        # Build for SSR
npm run serve:ssr        # Serve SSR build
npm run prerender        # Prerender static pages
```

### Store Code Structure

Module-based architecture (Angular 16):

```
store/src/app/
├── components/            # Reusable UI components
├── pages/                 # Page components
├── services/              # Application services
├── model/                 # TypeScript interfaces and types
├── app.module.ts          # Root module
└── app-routing.module.ts  # Route definitions
```

### Store Guidelines

- **Angular 16**: NgModules (traditional architecture)
- **SSR Support**: Server-side rendering enabled with `@nguniversal/express-engine`
- **State Management**: RxJS observables and services
- **Styling**: Bootstrap 5 + SCSS
- **Authentication**: Supabase for customer/guest authentication

## Testing

### Backend Tests (API)

- **Unit Tests**: Test services and use cases with mocked dependencies
  - Suffix: `*UT.java` (e.g., `CreateAccountServiceUT.java`)
  - Use `@ExtendWith(MockitoExtension.class)` and `@Mock`

- **Integration Tests**: Test full API flows with real database (Testcontainers)
  - Suffix: `*IT.java`
  - Use `@SpringBootTest` with `@Testcontainers`

```java
// Unit test example
@ExtendWith(MockitoExtension.class)
class CreateProductServiceUT {
    @Mock private ProductRepositoryPort repository;

    @Test
    @DisplayName("create - should return saved product")
    void create_shouldReturnSavedProduct() {
        // Given, When, Then
    }
}
```

### Frontend Tests

- **Panel**: Jasmine + Karma (Angular 17)
- **Store**: Jasmine + Karma (Angular 16)
- Tests colocated with components (`.spec.ts` files)
- Use `TestBed.configureTestingModule` for setup

## Documentation Structure

The `docs/` directory contains comprehensive project documentation organized in three phases:

### Phase 1: Analysis (What & Why)
- `business-context.md` - Business vision and rules
- `business-rules.md` - Cross-cutting business constraints
- `domain.md` - Ubiquitous language and domain model
- `modules/` - Detailed use cases per feature: `profiles`, `product-inventory`, `reservations-orders`, `subscriptions`, `profiles`, `clients`, `users`, `services`, `dashboard`
- `sprints/` - MVP targets and sprint planning
- `software-development-methods.md` - Scrum and incremental development approach

### Phase 2: Architecture (How)
- `system-architecture.md` - **READ THIS FIRST** - System design, tech stack, interaction flows
- `schema/database-schema.md` - PostgreSQL schema mapped to domain
- `enums/` - System-wide enumerations and state machines
- `diagrams/` - Visual architecture models

### Phase 3: Engineering (Implementation)
- `api-contracts/` - **Source of truth for backend** - REST endpoints, JSON structures, DTOs
- `bugs/` - Issue tracking and technical debt

**Important**: Always consult `docs/system-architecture.md` and relevant module docs in `docs/modules/` before making architectural decisions.

## Key Business Domain Concepts

- **Account**: Streaming service account (Netflix, Disney+, etc.) shared among users
- **Profile**: Individual seat/profile within an account (limited by account type)
- **Product**: Streaming service offering (platform + account type, e.g., "Netflix Premium")
- **Inventory**: Available products for sale with pricing and slot availability
- **Reservation**: Customer request to reserve a slot (pending payment)
- **Order**: Confirmed reservation after payment verification
- **Subscription**: Active subscription linking a user to an account slot with billing cycle
- **UserGuest**: Customer/guest who makes reservations

## Common Workflows

### Adding a New Backend Feature

1. Read relevant docs in `docs/modules/` and `docs/api-contracts/`
2. Create feature package: `api/src/main/java/com/neversion/api/<feature>/`
3. Implement domain layer first (models, ports)
4. Implement application layer (use cases)
5. Implement infrastructure layer (REST controllers, JPA repositories)
6. Write unit tests for services (`*UT.java`)
7. Write integration tests for controllers (`*IT.java`)
8. Update or create Flyway migration if database changes needed

### Adding a New Frontend Feature (Panel)

1. Create feature directory: `panel/src/app/features/<feature>/`
2. Generate component: `ng generate component features/<feature>/<name> --standalone`
3. Add route in `app.routes.ts` with lazy loading
4. Implement using Signals for state, Reactive Forms for inputs
5. Apply Bootstrap 5 utilities for styling
6. Add authentication guard if needed (`canActivate: [authGuard]`)
7. Write component tests (`.spec.ts`)

## Root Makefile

A `Makefile` at the project root serves as a command hub for common operations:

```bash
make help          # Show all available commands
make git-status    # Show git status
make git-commit MSG="message"  # Commit staged changes
make git-push BRANCH=main      # Push to remote
```

## Specialized Claude Code Agents

Three agents are available in `.claude/agents/` and are triggered automatically by task type:

- **`rest-adapter-engineer`**: REST controllers, SpringDoc/OpenAPI annotations, DTOs/request/response objects in the infrastructure adapter layer
- **`panel-ui-engineer`**: Visual consistency, design system, UX refinements in the Angular admin panel (presentation layer only)
- **`enum-manager`**: Adding/modifying enumerations across all layers — database migrations, Java enums, TypeScript constants

## Important Notes

- **Authentication**: All protected routes require JWT Bearer token from Supabase
- **Auth flow**: Frontend creates Supabase account → sends `externalId` (Supabase UUID) to backend (ADR-09 revised)
- **API Base URL**: All endpoints start with `/api/v1/`
- **Public identifier**: Always `id` in responses (maps to UUID — BIGINT never exposed)
- **Price naming**: `priceComplete` (not `priceFull`) — aligns with Glosario B.1 "Cuenta Completa"
- **CORS**: Configured in Spring Boot for frontend origins
- **Package Manager**: Use `pnpm` for panel, `npm` for store
- **Bitácora**: `/docs/implementation/backend-construction.md` — source of truth for project state
- **Git**: No git repository at the root level; use the root `Makefile` for cross-project commands

## OpenAPI Documentation

The API provides Swagger/OpenAPI documentation:
- Development: `http://localhost:8080/swagger-ui.html`
- API docs: `http://localhost:8080/v3/api-docs`

## Monitoring & Observability

Spring Boot Actuator endpoints available at `/actuator`:
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`
