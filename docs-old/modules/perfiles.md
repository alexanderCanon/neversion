# Perfiles

## Qué es

El **Perfil** (`profiles`) es la subdivisión física de una Cuenta (antes llamado "Slot"). Representa un espacio de acceso individual dentro de la plataforma de streaming — el perfil que el cliente ve y usa directamente.

Los perfiles son **generados automáticamente** al crear una Cuenta con `saleMode = BY_PROFILE`, basándose en el `max_profiles` definido en el Servicio. También pueden crearse manualmente.

Un perfil puede estar en tres estados (calculados, no persistidos):
- **`AVAILABLE`** — Sin suscripción activa, disponible para asignar.
- **`OCCUPIED`** — Tiene una suscripción activa o suspendida.
- **`BLOCKED`** — La suscripción expiró pero el slot no puede reasignarse sin intervención admin.

---

## Endpoints vigentes

Base path: `/api/v1/profiles`

### Listar perfiles de una cuenta

```
GET /api/v1/profiles?accountId={accountId}
```

| Param | Tipo | Requerido | Descripción |
|---|---|---|---|
| `accountId` | `long` | **Sí** | ID interno numérico de la cuenta |
| `available` | `boolean` | No | Filtrar solo perfiles disponibles (default: false) |

**Response `200 OK`** — Array de `ProfileResponse`

---

### Obtener perfil por UUID

```
GET /api/v1/profiles/{id}
```

| Código | Descripción |
|---|---|
| `200` | Encontrado |
| `404` | No encontrado |

---

### Crear perfil

```
POST /api/v1/profiles
```

Creación manual de un perfil.

**Request Body:**

```json
{
  "accountId": 1,
  "name": "Ariell Abrego",
  "pin": "1607",
  "isOwner": false
}
```

| Código | Descripción |
|---|---|
| `201` | Creado |
| `400` | Request inválido |

---

### Actualizar perfil

```
PUT /api/v1/profiles/{id}
```

Actualiza nombre, PIN o estado de owner.

**Request Body:**

```json
{
  "name": "Ariell Abrego Modificado",
  "pin": "1608",
  "isOwner": true
}
```

| Código | Descripción |
|---|---|
| `200` | Actualizado |
| `404` | No encontrado |

---

### Eliminar perfil

```
DELETE /api/v1/profiles/{id}
```

| Código | Descripción |
|---|---|
| `204` | Eliminado |
| `404` | No encontrado |

---

### Modelo de respuesta — `ProfileResponse`

```json
{
  "id": "uuid",
  "accountId": 1,
  "name": "Ariell Abrego",
  "pin": "1607",
  "isOwner": false,
  "createdAt": "2026-04-07T20:00:00"
}
```

---

## Esquema de base de datos

Tabla: `profiles`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `BIGINT` PK (secuencia) | Identificador interno |
| `uuid` | `UUID` UK | Identificador externo expuesto en el API |
| `account_id` | `BIGINT` FK → `accounts.id` ON DELETE CASCADE | Cuenta padre |
| `name` | `VARCHAR(100)` NOT NULL | Nombre configurado en la plataforma |
| `pin` | `VARCHAR(20)` | PIN de seguridad (nullable) |
| `is_owner` | `BOOLEAN` NOT NULL DEFAULT `false` | Si este perfil tiene privilegios de admin en la plataforma de streaming |
| `created_at` | `TIMESTAMPTZ` NOT NULL DEFAULT `now()` | |

> Ver esquema completo en [`../system/schema.md`](../system/schema.md)

---

## Reglas de negocio

- **BR-01 — Limitación de partición:** La cantidad de perfiles generados para una Cuenta depende del `max_profiles` de su Servicio padre. No se pueden crear más perfiles que ese límite.
- **BR-03 — Exclusividad del sale mode:** Una cuenta vendida como `FULL_ACCOUNT` tiene sus perfiles reservados para un único cliente — no se asignan individualmente.
- **BR-04 — Derivación de estado:** Si un perfil está "Disponible" se calcula consultando si existe una suscripción activa vinculada. No es un campo persistido en BD.

---

## Dependencias

| Módulo | Relación |
|---|---|
| [`cuentas.md`](cuentas.md) | Un perfil pertenece a una Cuenta (N:1) |
| [`subscripciones.md`](subscripciones.md) | Un perfil tiene máximo una suscripción activa (1:1 activa) |

---

## ⚠️ Notas para Claude Code

El esquema de base de datos usa Flyway con campos varchar. No existen enums a nivel de base de datos. Los enums presentes en el código Java son de la v1 y están deprecados para el esquema actual — no usarlos como referencia para migraciones ni queries.

- El `id` del perfil en la BD es `BIGINT` (secuencia, V4). El API expone el campo `uuid` como identificador externo.
- El parámetro `accountId` en `GET /profiles` es `long` (BIGINT en BD) — no UUID. Es el ID interno.
- Los estados `AVAILABLE`, `OCCUPIED`, `BLOCKED` son calculados en tiempo de consulta por el Application Service — no persisten en BD.
- `isOwner` mapea a `is_owner` en la tabla (snake_case en BD, camelCase en API).
- `pin` es nullable en la BD. El API puede retornarlo como `null` si no fue configurado.
- DELETE en profiles cascada automáticamente desde accounts (`ON DELETE CASCADE` en la FK `account_id`).
