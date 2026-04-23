# Clientes

## Qué es

El **Cliente** (`clients`) es el consumidor final que paga por acceso a un perfil de streaming o servicio digital. Esta entidad representa tanto a invitados no registrados durante el checkout de la tienda como a entidades gestionadas en el panel de administración.

> **Sprint 1.5:** El término legacy "Users" / "User Guests" ha sido completamente deprecado en favor del modelo unificado `clients`. Ver [`../archive/legacy/users.md`](../archive/legacy/users.md) para contexto histórico.

Un cliente puede tener múltiples suscripciones activas simultáneas (ej. una para Netflix, otra para Disney+) y puede iniciar múltiples reservaciones de checkout.

---

## Endpoints vigentes

Base path: `/api/v1/clients`

### Listar clientes

```
GET /api/v1/clients
```

| Param | Tipo | Requerido | Descripción |
|---|---|---|---|
| `name` | `string` | No | Filtrar por nombre |
| `phone` | `string` | No | Filtrar por número de teléfono |

**Response `200 OK`** — Array de `ClientResponse`

---

### Obtener cliente por ID

```
GET /api/v1/clients/{id}
```

| Código | Descripción |
|---|---|
| `200` | Encontrado |
| `404` | No encontrado |

---

### Crear cliente

```
POST /api/v1/clients
```

**Request Body:**

```json
{
  "name": "Ariell Abrego",
  "email": "ariell@example.com",
  "phone": "36137857"
}
```

| Código | Descripción |
|---|---|
| `201` | Creado |
| `400` | Request inválido |

---

### Actualizar cliente

```
PUT /api/v1/clients/{id}
```

**Request Body:**

```json
{
  "name": "Ariell Abrego Modificado",
  "email": "ariell.mod@example.com",
  "phone": "36137858"
}
```

| Código | Descripción |
|---|---|
| `200` | Actualizado exitosamente |
| `400` | Request inválido |
| `404` | No encontrado |

---

### Desactivar cliente

```
DELETE /api/v1/clients/{id}
```

Soft-delete. El registro se preserva para referencia histórica.

| Código | Descripción |
|---|---|
| `204` | Desactivado |
| `404` | No encontrado |

---

### Modelo de respuesta — `ClientResponse`

```json
{
  "id": "uuid",
  "name": "Ariell Abrego",
  "email": "ariell@example.com",
  "phone": "36137857"
}
```

---

## Esquema de base de datos

Tabla: `clients`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `BIGINT` PK (secuencia) | Identificador interno |
| `uuid` | `UUID` UK | Identificador externo expuesto en el API |
| `name` | `VARCHAR(255)` NOT NULL | Nombre completo |
| `phone` | `VARCHAR(30)` | Teléfono (WhatsApp para notificaciones) |
| `email` | `VARCHAR(255)` | Correo electrónico |
| `notes` | `TEXT` | Notas internas del admin |
| `created_at` | `TIMESTAMPTZ` NOT NULL DEFAULT `now()` | |

> Ver esquema completo en [`../system/schema.md`](../system/schema.md)

---

## Reglas de negocio

- Un cliente puede tener múltiples suscripciones activas simultáneas a diferentes servicios.
- El campo `phone` es el canal principal de contacto para automatizaciones de renovación (n8n via WhatsApp).
- Soft-delete preserva el historial de suscripciones y reservaciones asociadas.
- Sprint 1: Los clientes son gestionados exclusivamente por el Admin (el cliente no crea su propia cuenta).
- Sprint 2 (futuro): Se introduce login de cliente via Supabase Auth.

---

## Dependencias

| Módulo | Relación |
|---|---|
| [`subscripciones.md`](subscripciones.md) | Un cliente tiene muchas suscripciones (1:N) |
| [`reservaciones.md`](reservaciones.md) | Un cliente puede iniciar múltiples reservaciones (1:N) |

---

## ⚠️ Notas para Claude Code

El esquema de base de datos usa Flyway con campos varchar. No existen enums a nivel de base de datos. Los enums presentes en el código Java son de la v1 y están deprecados para el esquema actual — no usarlos como referencia para migraciones ni queries.

- El `id` interno en la BD es `BIGINT` (generado con secuencia, V4). El API expone el campo `uuid`.
- Soft-delete implementado via `@SQLDelete` y `@SQLRestriction` en la entidad JPA.
- Los autofiltros de nombre/teléfono son opcionales; el backend retorna todos los clientes si no se pasan parámetros.
- `phone` y `email` son nullable en la BD (sin `NOT NULL`). El API sí puede requerirlos vía validación.
