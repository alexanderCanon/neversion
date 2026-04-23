# Neversion Docs — Índice

> **Fuente de verdad del sistema Neversion.** Este índice es el punto de entrada único para navegar toda la documentación técnica y operativa del proyecto.

---

## ⚡ FAQ — Preguntas frecuentes

Las 5 preguntas que todo colaborador o agente de IA hace primero:

| # | Pregunta | Ir a |
|---|---|---|
| 1 | ¿Qué tablas existen en la base de datos y qué campos tienen? | [`system/schema.md`](system/schema.md) |
| 2 | ¿Cómo está estructurada la arquitectura del backend (hexagonal)? | [`system/architecture.md — Sección 2`](system/architecture.md#2-backend--arquitectura-hexagonal-ports--adapters-y-ddd) |
| 3 | ¿Cuáles son los endpoints REST disponibles para un módulo específico? | [`modules/<nombre-modulo>.md — Sección Endpoints`](modules/) |
| 4 | ¿Cómo se manejan los errores HTTP? ¿Qué formato tienen? | [`system/api-conventions.md — Sección 3`](system/api-conventions.md#3-manejo-de-errores--rfc-7807-problem-details) |
| 5 | ¿Los enums del código Java están vigentes en la BD? | [`archive/enums-v1/enums.md`](archive/enums-v1/enums.md) — **No. Ver ⚠️ en cada módulo.** |

---

## Capítulo 1 — `system/` — Especificación Técnica

Documentación permanente del sistema. Leer antes de implementar cualquier feature.

| Archivo | Contenido | Cuándo leerlo |
|---|---|---|
| [`system/overview.md`](system/overview.md) | Qué es Neversion, dominio, glosario de entidades, jerarquía | Primera exploración del repo |
| [`system/architecture.md`](system/architecture.md) | Hexagonal Architecture, diagramas de capas, Angular 17, auth flow OAuth2, metodologías | Antes de contribuir al backend o frontend |
| [`system/stack.md`](system/stack.md) | Tech stack completo con versiones y comandos de desarrollo | Configuración de entorno, agregar dependencias |
| [`system/schema.md`](system/schema.md) | Esquema de BD unificado (Sprint 1.5), diagrama ER, descripción de tablas y campos | Antes de escribir migraciones Flyway o queries |
| [`system/api-conventions.md`](system/api-conventions.md) | Principios API (Contract-First), versionado, REST semántico, RFC 7807 errors, HTTP status codes | Antes de implementar cualquier endpoint |

---

## Capítulo 2 — `modules/` — Módulos de Dominio y Contratos

Cada archivo consolida: definición del dominio + endpoints REST + schema DB + reglas de negocio.

| Archivo | Entidad principal | Tabla BD |
|---|---|---|
| [`modules/clientes.md`](modules/clientes.md) | Consumidor final del servicio digital | `clients` |
| [`modules/cuentas.md`](modules/cuentas.md) | Credencial maestra comprada al mayorista | `accounts` |
| [`modules/perfiles.md`](modules/perfiles.md) | Subdivisión de una cuenta (perfil individual) | `profiles` |
| [`modules/subscripciones.md`](modules/subscripciones.md) | Vínculo activo: cliente ↔ perfil + fechas de pago | `subscriptions` |
| [`modules/servicios.md`](modules/servicios.md) | Plataforma digital ofrecida (Netflix, Spotify…) | `services` |
| [`modules/reservaciones.md`](modules/reservaciones.md) | Checkout temporal, expira en 60 min | `reservations` |
| [`modules/ordenes.md`](modules/ordenes.md) | Registro permanente creado al validar una reservación | `orders` |

**Estructura fija de cada módulo:**

1. Qué es
2. Endpoints vigentes
3. Esquema de base de datos
4. Reglas de negocio
5. Dependencias
6. ⚠️ Notas para Claude Code

---

## Capítulo 3 — `archive/` — Histórico y Deprecado

Documentación que ya no es vigente pero se preserva para contexto y auditoría.

| Ruta | Contenido | Por qué está aquí |
|---|---|---|
| [`archive/enums-v1/enums.md`](archive/enums-v1/enums.md) | Definición de enums del sistema (v1) | Deprecados para el esquema actual. BD usa `varchar`, no enums de PostgreSQL. |
| [`archive/legacy/product-inventory.md`](archive/legacy/product-inventory.md) | Módulo Products + Inventory (legacy) | Reemplazado por `services` en Sprint 1.5 |
| [`archive/legacy/users.md`](archive/legacy/users.md) | Módulo Users / User Guests (legacy) | Reemplazado por `clients` en Sprint 1.5 |
| `archive/legacy/uiux/` | Revisiones de UI/UX históricas | Sin destino activo en la nueva estructura |
| `archive/legacy/` (otros) | Índices y validaciones antiguas de API contracts | Consolidados en `system/api-conventions.md` y `modules/` |

> [!WARNING]
> Los enums en `archive/enums-v1/` describen una versión anterior del esquema que usaba `TYPE AS ENUM` en PostgreSQL. El esquema actual usa `varchar`. No usar estos enums como referencia para migraciones Flyway ni queries JPA.

---

## Separación clara: `docs/` vs `project/`

| Directorio | Propósito | Contenido |
|---|---|---|
| **`docs/`** | Especificación técnica vigente del sistema | `system/`, `modules/`, `archive/` |
| **`project/`** | Gestión operativa del proyecto | Sprints, bugs, vault, prompts |

### `project/` — Gestión Operativa

| Carpeta | Contenido |
|---|---|
| [`project/sprints/`](../project/sprints/) | Definición de sprints, scope, roadmap, session summaries |
| [`project/bugs/`](../project/bugs/) | Registro de bugs por mes |
| [`project/vault/`](../project/vault/) | Brainstorming, ideas, refactoring notes (Obsidian raw) |
| [`project/prompt/`](../project/prompt/) | Historial de prompts usados con IAs (auditoría) |

---

*Última actualización: Sprint 1.5 — Abril 2026*
