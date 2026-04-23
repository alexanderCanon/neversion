# Neversion — Tech Stack

> Referencia rápida del stack tecnológico completo del sistema.

---

## Backend (`api/`)

| Tecnología | Versión | Rol |
|---|---|---|
| **Java** | 17 | Lenguaje principal |
| **Spring Boot** | 3.x | Framework de aplicación |
| **Spring Security** | (via Boot) | OAuth2 Resource Server, validación JWT |
| **Spring Data JPA** | (via Boot) | ORM y repositorios |
| **Hibernate** | (via Boot) | Implementación JPA |
| **PostgreSQL** | — | Base de datos relacional |
| **Flyway** | — | Migraciones de esquema de BD |
| **AWS S3** | — | Almacenamiento de imágenes de recibos |
| **Testcontainers** | — | Tests de integración con contenedores reales |
| **JUnit 5** | — | Framework de testing unitario |
| **Mockito** | — | Mocks para pruebas unitarias |
| **Maven** | `./mvnw` | Gestor de dependencias y builds |

**Comandos:**

```bash
./mvnw spring-boot:run     # Iniciar servidor
./mvnw clean package       # Build del JAR
./mvnw test                # Ejecutar todos los tests
```

---

## Admin Panel (`panel/`)

| Tecnología | Versión | Rol |
|---|---|---|
| **Angular** | 17 | Framework frontend principal |
| **TypeScript** | — | Lenguaje del frontend |
| **Bootstrap** | 5 | Sistema de estilos (utility-first) |
| **SCSS + OKLCH** | — | Variables de color y estilos customizados |
| **pnpm** | — | Gestor de paquetes (más rápido que npm) |
| **Supabase JS** | — | Autenticación del lado cliente |

**Patrones:**

- Standalone Components (sin NgModule)
- Angular Signals (`signal()`, `computed()`, `effect()`)
- Reactive Forms
- Smart / Dumb component split
- Feature-based architecture

**Comandos:**

```bash
pnpm install       # Instalar dependencias
pnpm start         # Servidor de desarrollo
pnpm run build     # Build de producción
pnpm test          # Tests unitarios
pnpm run lint      # Lint del código
```

---

## Storefront (`store/`)

| Tecnología | Versión | Rol |
|---|---|---|
| **Angular** | 16 | Framework frontend cliente |
| **npm** | — | Gestor de paquetes |

**Comandos:**

```bash
npm install        # Instalar dependencias
npm start          # Servidor de desarrollo
npm run build      # Build de producción
```

---

## Servicios externos

| Servicio | Rol |
|---|---|
| **Supabase Auth** | Identity Provider (OAuth2/JWT). El panel se autentica aquí. Spring Boot valida firmas via JWKS. |
| **Supabase PostgreSQL** | Base de datos gestionada en la nube |
| **AWS S3** | Almacenamiento de comprobantes de pago (receipt images) |
| **n8n** | Automatización de notificaciones de renovación (WhatsApp/Email). Consulta `subscriptions` y escribe en `notification_log`. |

---

## Cuándo leer este archivo

- Al configurar el entorno de desarrollo por primera vez
- Para verificar versiones de tecnología antes de agregar dependencias
- Para entender qué servicios externos se requieren en producción
