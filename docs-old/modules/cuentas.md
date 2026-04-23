# Cuentas

## Qué es

La **Cuenta** (`accounts`) es la credencial maestra comprada por el Admin a un proveedor mayorista para operar un servicio digital específico. Contiene las credenciales de acceso al proveedor (`email`, `password`), la fecha de renovación con el mayorista (`renewal_date`), y el modo de venta (`sale_mode`).

Una cuenta puede venderse de dos formas:
- **`BY_PROFILE`** — Se subdivide en múltiples perfiles individuales, cada uno asignado a un cliente diferente.
- **`FULL_ACCOUNT`** — Se vende completa a un único cliente que ocupa todos los perfiles.

Al crear una cuenta con `saleMode = BY_PROFILE`, el sistema **genera automáticamente** los perfiles según el `maxProfiles` definido en el Servicio padre.

---

## Endpoints vigentes

Base path: `/api/v1/accounts`

### Listar cuentas

```
GET /api/v1/accounts
```

| Param | Tipo | Requerido | Descripción |
|---|---|---|---|
| `serviceId` | `integer` | No | Filtrar por servicio (ID interno) |

**Response `200 OK`** — Array de `AccountResponse`

---

### Obtener cuenta por ID

```
GET /api/v1/accounts/{id}
```

| Código | Descripción |
|---|---|
| `200` | Encontrada |
| `404` | No encontrada |

---

### Crear cuenta

```
POST /api/v1/accounts
```

Registra una nueva credencial maestra. Genera perfiles automáticamente si `saleMode = BY_PROFILE`.

**Request Body:**

```json
{
  "email": "nexp1010+a100@gmail.com",
  "pass": "588netflix",
  "serviceId": 1,
  "saleMode": "BY_PROFILE",
  "renewalDate": "2026-05-01",
  "notes": "Purchased from ProviderX"
}
```

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `email` | `string` | Sí | Credencial email del proveedor |
| `pass` | `string` | Sí | Contraseña del proveedor |
| `serviceId` | `integer` | Sí | ID interno del servicio vinculado |
| `saleMode` | `string` | Sí | `BY_PROFILE` o `FULL_ACCOUNT` |
| `renewalDate` | `string (date)` | Sí | Fecha en que Neversion renueva con el mayorista |
| `notes` | `string` | No | Notas internas del admin |

| Código | Descripción |
|---|---|
| `201` | Cuenta creada |
| `400` | Request inválido |

---

### Eliminar cuenta

```
DELETE /api/v1/accounts/{id}
```

Hard-delete. Cascada a perfiles.

| Código | Descripción |
|---|---|
| `204` | Eliminada |
| `404` | No encontrada |

---

### Modelo de respuesta — `AccountResponse`

```json
{
  "id": "uuid",
  "email": "nexp1010+a100@gmail.com",
  "pass": "588netflix",
  "serviceId": 1,
  "saleMode": "BY_PROFILE",
  "renewalDate": "2026-05-01",
  "notes": "Purchased from ProviderX",
  "createdAt": "2026-03-15T10:00:00"
}
```

> **Nota:** `pass` se retorna en texto plano intencionalmente. Son credenciales de reventa manejadas por el admin, no contraseñas de autenticación de usuarios.

`saleMode` valores: `BY_PROFILE`, `FULL_ACCOUNT`

---

## Esquema de base de datos

Tabla: `accounts`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `BIGINT` PK (secuencia) | Identificador interno |
| `uuid` | `UUID` UK | Identificador externo expuesto en el API |
| `service_id` | `BIGINT` FK → `services.id` | Servicio al que pertenece |
| `email` | `VARCHAR(255)` NOT NULL | Correo del proveedor |
| `password` | `VARCHAR(255)` NOT NULL | Contraseña del proveedor (plaintext) |
| `renewal_date` | `DATE` NOT NULL | Fecha de renovación con el mayorista |
| `plan` | `VARCHAR(100)` | Tier de calidad (ej. "4K Ultra HD") |
| `sale_mode` | `VARCHAR(20)` NOT NULL DEFAULT `'by_profile'` | `by_profile` o `full_account` |
| `notes` | `TEXT` | Notas internas del admin |
| `created_at` | `TIMESTAMPTZ` NOT NULL DEFAULT `now()` | |

> Ver esquema completo en [`../system/schema.md`](../system/schema.md)

---

## Reglas de negocio

- **BR-01 — Limitación de partición:** La cantidad de perfiles generados automáticamente depende del `max_profiles` definido en el Servicio padre.
- **BR-03 — Exclusividad del sale mode:** Una cuenta vendida como `FULL_ACCOUNT` asume control directo sobre todos sus perfiles lógicos — no se pueden asignar individualmente.
- **BR-04 — Disponibilidad derivada:** Si una cuenta o perfil está "disponible" se calcula consultando si existen suscripciones activas vinculadas. No es un campo persistido.
- El campo `password` se almacena en texto plano (credenciales de reventa, no de autenticación).

---

## Dependencias

| Módulo | Relación |
|---|---|
| [`servicios.md`](servicios.md) | Una cuenta pertenece a un Servicio (N:1) |
| [`perfiles.md`](perfiles.md) | Una cuenta contiene múltiples perfiles (1:N) |
| [`subscripciones.md`](subscripciones.md) | Las suscripciones referencian cuentas vía perfil |

---

## ⚠️ Notas para Claude Code

El esquema de base de datos usa Flyway con campos varchar. No existen enums a nivel de base de datos. Los enums presentes en el código Java son de la v1 y están deprecados para el esquema actual — no usarlos como referencia para migraciones ni queries.

- `sale_mode` es `varchar` en la BD con valores en **`lowercase`**: `by_profile`, `full_account`. El API los expone en `UPPER_CASE`. Verificar el mapper.
- La generación automática de perfiles ocurre en el Application Service al crear la cuenta, no en la BD.
- Hard-delete en cuentas cascada a perfiles via `ON DELETE CASCADE` en la FK de `profiles.account_id`.
- El campo `pass` (en el request del API) mapea a `password` en la tabla. Verificar el `RequestMapper`.
- `id` interno es `BIGINT` (secuencia, V4). El API expone el campo `uuid`.
