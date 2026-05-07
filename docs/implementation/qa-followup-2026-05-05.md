# QA Follow-up — 2026-05-05

Este documento concentra hallazgos detectados durante revisión manual de tablas y pruebas funcionales del panel. No representa cambios implementados; es backlog operativo para atacar en la siguiente sesión.

## Prioridad Alta

### 1. Cuentas `FULL_ACCOUNT` permiten perfiles extra

**Módulo:** API + Panel

**Hallazgo:** Una cuenta creada con `sale_mode = FULL_ACCOUNT` debería tener exactamente un perfil dueño (`is_owner = true`) como ancla técnica de la suscripción. En DB se observó una cuenta `FULL_ACCOUNT` con dos perfiles: uno dueño y otro no dueño, ambos disponibles.

**Causa probable:** El backend crea correctamente un perfil dueño al registrar la cuenta, pero el endpoint de generación manual de perfiles permite generar perfiles para cualquier cuenta mientras no se exceda `service.maxProfiles`.

**Riesgo:** Inventario inconsistente. Una cuenta completa puede terminar comportándose como cuenta por perfiles, afectando asignaciones, disponibilidad, KPIs y ciclo de vida de suscripciones.

**Propuesta de fix:**
- Bloquear `ProfileService.generate(...)` cuando la cuenta tenga `saleMode = FULL_ACCOUNT`.
- Ocultar o deshabilitar la sección “Generación de Perfiles” en el panel para cuentas `FULL_ACCOUNT`.
- Agregar prueba backend para validar que `FULL_ACCOUNT` rechaza generación manual.

### 2. `saleMode` de cuenta es mutable

**Módulo:** API

**Hallazgo:** `UpdateAccountService` permite cambiar `saleMode` mediante `PUT /accounts/{id}`.

**Riesgo:** Cambiar una cuenta de `BY_PROFILE` a `FULL_ACCOUNT`, o viceversa, puede dejar perfiles y suscripciones existentes en estados incompatibles con la modalidad actual.

**Propuesta de fix:**
- Hacer `saleMode` inmutable después de la creación de la cuenta.
- Si `updates.saleMode != existing.saleMode`, devolver `BusinessRuleException`.
- Revisar si `serviceId` también debería ser inmutable cuando ya existen perfiles o suscripciones.

### 3. Dashboard del vendedor no carga

**Módulo:** API + Panel

**Síntoma:** El panel muestra: “No se pudo cargar el dashboard del vendedor. Intente nuevamente”.

**Causa pendiente de confirmar:** Puede ser uno de estos patrones ya detectados:
- Query backend con filtros opcionales nulos que PostgreSQL no puede tipar.
- Cliente OpenAPI usando `Accept: */*` y procesando JSON como `Blob`.
- Contrato frontend/backend no consistente.
- Error real de agregación KPI.

**Propuesta de investigación:**
- Revisar Network y logs backend de todos los endpoints usados por Dashboard.
- Revisar `DashboardApiService` generado y wrappers del panel.
- Forzar `Accept: application/json` en llamadas que devuelvan JSON.
- Corregir queries backend que usen `:param IS NULL` con parámetros opcionales.

### 4. Reservas y Órdenes devuelven 500 al entrar

**Módulo:** API + Panel

**Síntoma:** Al entrar a Reservas y Órdenes se reciben errores 500.

**Causa pendiente de confirmar:** Posible repetición del patrón encontrado en Clientes, Servicios, Cuentas y Suscripciones: queries con filtros opcionales nulos o contratos mal interpretados por cliente OpenAPI.

**Propuesta de investigación:**
- Capturar logs backend exactos para Reservas y Órdenes.
- Revisar repositorios JPA/native queries con parámetros opcionales.
- Revisar servicios Angular para base URL, vendor UUID, normalización de arrays y `Accept`.
- Ajustar manejo de errores para que validaciones o ownership no terminen como 500.

## Prioridad Media

### 5. Contrato de acceso en detalle de suscripción

**Módulo:** API + Panel + Store

**Hallazgo:** La relación interna existe:

```text
subscriptions.profile_id
  -> profiles.id
  -> profiles.account_id
  -> accounts.id
  -> accounts.email + accounts.password
```

Para cliente ya existe:

```http
GET /api/v1/clients/me/accesses
```

Este endpoint retorna `accountEmail`, `accountPassword`, `profileName`, `profilePin`, `paymentDueDate` y `status`.

**Brecha:** `SubscriptionDetailResponse` del panel vendedor incluye `account.email` y `profile.pin`, pero no incluye `account.password`.

**Riesgo:** El vendedor no puede ver desde el detalle de suscripción el acceso completo que está ligado a esa suscripción.

**Propuesta de contrato:**

```json
{
  "access": {
    "accountEmail": "...",
    "accountPassword": "...",
    "profileName": "...",
    "profilePin": "...",
    "saleMode": "BY_PROFILE"
  }
}
```

**Nota:** Preferir un bloque explícito `access` en vez de ensuciar `AccountSummary`. El dominio define que la cuenta es email + contraseña y el cliente debe poder consultar sus accesos permanentemente.

### 6. Revisar `GET /clients/me/accesses` en frontend cliente

**Módulo:** Store / API Client

**Hallazgo:** El cliente OpenAPI generado para `getMyAccesses()` usa `Accept: */*` por defecto.

**Riesgo:** Puede repetirse el bug donde Angular recibe JSON como `Blob` y el pipeline entrega `{}` o datos no iterables.

**Propuesta de fix:**
- Cuando se implemente o revise el panel cliente, forzar `Accept: application/json`.
- Normalizar respuesta a array antes de renderizar.
- Validar estados: `ACTIVE` devuelve credenciales; `SUSPENDED` devuelve credenciales `null`.

### 7. Campo `services.details` JSON/JSONB

**Módulo:** API + DB + Docs

**Hallazgo:** La tabla `services` tiene un campo `details` de tipo JSON/JSONB. Falta confirmar por qué se definió y si se usa actualmente.

**Propuesta de investigación:**
- Revisar migraciones, entidad `ServiceEntity`, DTOs y mappers.
- Verificar si `details` está expuesto en OpenAPI.
- Decidir si se conserva como metadata flexible o si debe eliminarse/documentarse.

### 8. Imagen de servicio como upload real

**Módulo:** Panel + Supabase Storage + API

**Hallazgo:** Al crear servicio se pide la imagen como URL manual.

**Mejora requerida:** Implementar carga de imagen desde el panel.

**Flujo propuesto:**
- Usuario selecciona imagen.
- Panel valida tamaño, MIME y dimensiones recomendadas.
- Panel sube a Supabase Storage.
- Supabase devuelve URL pública o firmada.
- Panel envía esa URL en `imageUrl`.
- Backend guarda `imageUrl` en DB.

**Decisión pendiente:**
- Bucket público si son imágenes de catálogo.
- Bucket privado con signed URLs si se decide proteger assets.

### 9. Semántica de `accounts.source`

**Módulo:** API + Panel + Docs

**Hallazgo:** `source` significa proveedor/origen de adquisición de la cuenta. Puede confundirse con método de pago.

**Propuesta:**
- Documentar `source` como proveedor/origen.
- Si el negocio necesita método de pago de compra de cuenta, crear otro campo, por ejemplo `purchasePaymentMethod`.

## Calidad Transversal

### 10. Manejo de excepciones y códigos HTTP

**Módulo:** API

**Hallazgo:** Varios errores funcionales están llegando como 500.

**Propuesta de estándar:**
- `400 Bad Request`: request inválido o regla de negocio violada.
- `401 Unauthorized`: sin autenticación.
- `403 Forbidden`: usuario autenticado sin ownership/permisos.
- `404 Not Found`: recurso inexistente real.
- `409 Conflict`: conflicto de estado/inventario cuando aplique.
- `500 Internal Server Error`: solo fallas inesperadas reales.

**Acción:** Revisar `GlobalHandlerException` y mapear excepciones de dominio/persistencia esperadas.

### 11. Patrón OpenAPI `Accept: */*`

**Módulo:** Panel + Store + API Client

**Hallazgo:** El cliente Angular generado por OpenAPI selecciona `responseType` según el header `Accept`. Cuando el contrato generado solo declara `*/*`, Angular puede tratar respuestas JSON como `Blob`.

**Riesgo:** Listados vacíos, objetos no iterables o respuestas `{}` aunque el backend responda 200 con JSON válido.

**Propuesta:**
- En wrappers Angular, siempre pasar `{ httpHeaderAccept: 'application/json' }` para endpoints que devuelvan JSON.
- Normalizar arrays cuando el backend pueda devolver lista directa o página `{ content: [] }`.
- Revisar si la causa raíz está en la configuración OpenAPI del backend: produces/content type debe declarar `application/json`.

## Orden sugerido para mañana

1. Dashboard: capturar logs y corregir causa raíz.
2. Reservas y Órdenes: confirmar si repiten patrón de queries opcionales nulas.
3. Reglas de cuentas/perfiles: bloquear perfiles extra en `FULL_ACCOUNT` y hacer `saleMode` inmutable.
4. Contrato de detalle de suscripción: decidir bloque `access` para vendedor.
5. Revisar `GET /clients/me/accesses` en Store con `Accept: application/json`.
6. Investigar `services.details`.
7. Diseñar upload de imágenes a Supabase Storage.
