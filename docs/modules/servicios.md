# Servicios

## Qué es

El **Servicio** (`services`) es la plataforma digital ofrecida por Neversion (ej. Netflix, Spotify, Disney+). Es el anchor point del catálogo — actúa como categoría y define las reglas estructurales que gobiernan las cuentas compradas bajo él.

El Servicio no tiene precio ni duración directamente. Esa información se almacena en el campo JSONB `details` (pricing tiers, moneda, metadata flexible) o se maneja a nivel de cuenta/suscripción.

> **Sprint 1.5:** El modelo legacy `products` + `inventory` ha sido deprecado y consolidado en `services`. Ver [`../archive/legacy/product-inventory.md`](../archive/legacy/product-inventory.md) para contexto histórico.

---

## Endpoints vigentes

Base path: `/api/v1/services`

### Listar todos los servicios

```
GET /api/v1/services
```

**Auth:** Público — no requiere token.

**Response `200 OK`** — Array de `ServiceResponse`

---

### Obtener servicio por ID

```
GET /api/v1/services/{id}
```

`{id}` es un UUID (identificador externo).

**Auth:** Público — no requiere token.

| Código | Descripción |
|---|---|
| `200` | Encontrado |
| `404` | No encontrado |

---

### Crear servicio

```
POST /api/v1/services
```

**Auth:** Rol `ADMIN` requerido.

**Request Body:**

```json
{
  "name": "Netflix",
  "maxProfiles": 5,
  "details": "{\"pricing\": [{\"duration_days\": 30, \"price\": 50.00, \"currency\": \"GTQ\"}]}"
}
```

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `name` | `string` | Sí | Nombre único de la plataforma |
| `maxProfiles` | `integer` | No | Límite de perfiles por cuenta (BR-01) |
| `details` | `string (JSON)` | No | JSONB serializado: pricing tiers, moneda, metadata |

| Código | Descripción |
|---|---|
| `201` | Creado |
| `400` | Error de validación o nombre duplicado |

---

### Actualizar servicio

```
PUT /api/v1/services/{id}
```

**Auth:** Rol `ADMIN` requerido.

Mismo body que crear.

| Código | Descripción |
|---|---|
| `200` | Actualizado |
| `400` | Error de validación |
| `404` | No encontrado |

---

### Eliminar servicio

```
DELETE /api/v1/services/{id}
```

**Auth:** Rol `ADMIN` requerido.

| Código | Descripción |
|---|---|
| `204` | Eliminado |
| `404` | No encontrado |

---

### Modelo de respuesta — `ServiceResponse`

```json
{
  "id": "uuid",
  "name": "Netflix",
  "maxProfiles": 5,
  "details": "{\"pricing\": [{\"duration_days\": 30, \"price\": 50.00, \"currency\": \"GTQ\"}]}",
  "createdAt": "2026-04-07T10:00:00"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador externo — usar este en todas las referencias frontend |
| `name` | `string` | Nombre de la plataforma |
| `maxProfiles` | `integer` | Límite de perfiles por cuenta |
| `details` | `string (JSON)` | Metadata JSONB serializada como string |
| `createdAt` | `datetime` | Timestamp de creación |

---

## Esquema de base de datos

Tabla: `services`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `BIGINT` PK (secuencia) | Identificador interno |
| `uuid` | `UUID` UK | Identificador externo expuesto en el API |
| `name` | `VARCHAR(150)` NOT NULL UNIQUE | Nombre único de la plataforma |
| `max_profiles` | `INT` NOT NULL | Límite de perfiles por cuenta |
| `category` | `VARCHAR(50)` NOT NULL DEFAULT `'STREAMING'` | Categoría para filtrado del dashboard (agregado en V5) |
| `details` | `JSONB` | Metadata flexible (pricing tiers, etc.) |
| `created_at` | `TIMESTAMPTZ` NOT NULL DEFAULT `now()` | |

> Ver esquema completo en [`../system/schema.md`](../system/schema.md)

---

## Reglas de negocio

- **BR-17 — Nombre único:** El `name` del servicio debe ser único en todo el catálogo. Un intento de crear un servicio con nombre duplicado retorna `400`.
- **BR-18 — Acceso compartido:** No existen restricciones de ownership a nivel de servicio. Todos los admins comparten acceso global para modificar y mantener el catálogo.
- **BR-01 — maxProfiles como gobernador:** El campo `maxProfiles` del servicio determina cuántos perfiles se generan automáticamente al crear una Cuenta con `saleMode = BY_PROFILE`.

---

## Dependencias

| Módulo | Relación |
|---|---|
| [`cuentas.md`](cuentas.md) | Un servicio tiene múltiples cuentas (1:N) |

---

## ⚠️ Notas para Claude Code

El esquema de base de datos usa Flyway con campos varchar. No existen enums a nivel de base de datos. Los enums presentes en el código Java son de la v1 y están deprecados para el esquema actual — no usarlos como referencia para migraciones ni queries.

- El `id` interno en la BD es `BIGINT` con secuencia (V4). El API expone el campo `uuid` como identificador externo. Asegurarse de que el `ResponseMapper` convierte correctamente.
- El campo `details` es `JSONB` en PostgreSQL pero se serializa/deserializa como `String` en el API — no como objeto JSON estructurado.
- Los endpoints de lista y detalle son **públicos** (sin autenticación). Solo create/update/delete requieren rol `ADMIN`.
- `max_profiles` puede ser `-1` o `null` si el servicio no tiene un límite definido — verificar con el Application Service.
- El campo `category` fue agregado en **V5** con `DEFAULT 'STREAMING'`. Todos los servicios anteriores tienen `STREAMING` como categoría. Usar el enum `CategoryType` (en el Application Layer) para validar los valores posibles antes de persistir.
