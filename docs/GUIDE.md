# Documentation Quick Guide (Neversion)

Bienvenido al sistema de documentación de Neversion. Esta guía es tu mapa rápido para navegar el repositorio sin perderte.

> [!TIP]
> Si eres un agente de IA, lee este archivo primero para mapear el repositorio antes de explorar archivos específicos. Para preguntas frecuentes e índice completo, ve directamente a **[`INDEX.md`](INDEX.md)**.

---

## Estructura de documentación

La documentación está dividida en **dos capas** con propósitos distintos:

```
docs/          ← Especificación técnica VIGENTE del sistema
project/       ← Gestión operativa del proyecto (sprints, bugs, vault)
```

---

## `docs/` — Especificación Técnica

### `system/` — Referencia del sistema

| Archivo | Qué contiene |
|---|---|
| [`system/overview.md`](system/overview.md) | Qué es Neversion, dominio, glosario de entidades |
| [`system/architecture.md`](system/architecture.md) | Hexagonal Architecture, diagramas, Angular 17, auth flow |
| [`system/stack.md`](system/stack.md) | Tech stack completo con versiones y comandos |
| [`system/schema.md`](system/schema.md) | Esquema de BD, diagrama ER, descripción de tablas |
| [`system/api-conventions.md`](system/api-conventions.md) | Principios API, versionado, RFC 7807, HTTP status codes |

### `modules/` — Módulos de dominio y contratos REST

Cada archivo consolida dominio + endpoints + schema + reglas de negocio. Estructura fija en todos.

| Archivo | Entidad |
|---|---|
| [`modules/clientes.md`](modules/clientes.md) | Consumidores finales (`clients`) |
| [`modules/cuentas.md`](modules/cuentas.md) | Credenciales maestras del mayorista (`accounts`) |
| [`modules/perfiles.md`](modules/perfiles.md) | Subdivisiones de cuenta (`profiles`) |
| [`modules/subscripciones.md`](modules/subscripciones.md) | Vínculo cliente ↔ perfil + pagos (`subscriptions`) |
| [`modules/servicios.md`](modules/servicios.md) | Plataformas digitales ofrecidas (`services`) |
| [`modules/reservaciones.md`](modules/reservaciones.md) | Checkout temporal (`reservations`) |
| [`modules/ordenes.md`](modules/ordenes.md) | Registro de compra validada (`orders`) |

### `archive/` — Histórico y deprecado

| Ruta | Qué hay |
|---|---|
| [`archive/enums-v1/`](archive/enums-v1/) | Enums de la v1 — **DEPRECADOS** en el esquema actual |
| [`archive/legacy/`](archive/legacy/) | Módulos reemplazados (products, users), índices obsoletos, uiux |

---

## `project/` — Gestión Operativa

| Carpeta | Qué contiene |
|---|---|
| [`project/sprints/`](../project/sprints/) | Sprints, ajustes de scope, roadmap, session summaries |
| [`project/bugs/`](../project/bugs/) | Registro de bugs por mes |
| [`project/vault/`](../project/vault/) | Brainstorming y notas crudas (Obsidian) |
| [`project/prompt/`](../project/prompt/) | Historial de prompts usados con IAs |

---

## ¿Por dónde empezar?

| Situación | Ir a |
|---|---|
| Primera exploración del repositorio | [`system/overview.md`](system/overview.md) |
| Índice completo con FAQ | [`INDEX.md`](INDEX.md) |
| Implementar un endpoint nuevo | Módulo en [`modules/`](modules/) → luego [`system/api-conventions.md`](system/api-conventions.md) |
| Escribir una migración Flyway | [`system/schema.md`](system/schema.md) |
| Entender el sprint actual | [`project/sprints/`](../project/sprints/) |