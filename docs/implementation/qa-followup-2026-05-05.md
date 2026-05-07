# QA Follow-up — 2026-05-05

Este documento concentra hallazgos detectados durante revisión manual de tablas y pruebas funcionales del panel. No representa cambios implementados; es backlog operativo para atacar en la siguiente sesión.

## Prioridad Alta

### 1. Cuentas `FULL_ACCOUNT` permiten perfiles extra

**Módulo:** API + Panel

**Estado 2026-05-07:** Corregido.

**Hallazgo:** Una cuenta creada con `sale_mode = FULL_ACCOUNT` debería tener exactamente un perfil dueño (`is_owner = true`) como ancla técnica de la suscripción. En DB se observó una cuenta `FULL_ACCOUNT` con dos perfiles: uno dueño y otro no dueño, ambos disponibles.

**Causa probable:** El backend crea correctamente un perfil dueño al registrar la cuenta, pero el endpoint de generación manual de perfiles permite generar perfiles para cualquier cuenta mientras no se exceda `service.maxProfiles`.

**Riesgo:** Inventario inconsistente. Una cuenta completa puede terminar comportándose como cuenta por perfiles, afectando asignaciones, disponibilidad, KPIs y ciclo de vida de suscripciones.

**Propuesta de fix:**
- Bloquear `ProfileService.generate(...)` cuando la cuenta tenga `saleMode = FULL_ACCOUNT`.
- Ocultar o deshabilitar la sección “Generación de Perfiles” en el panel para cuentas `FULL_ACCOUNT`.
- Agregar prueba backend para validar que `FULL_ACCOUNT` rechaza generación manual.

**Resultado:** `ProfileService.generate(...)` ahora lanza `BusinessRuleException` para cuentas `FULL_ACCOUNT` antes de consultar límites del servicio o guardar perfiles. El panel pasa `canGenerateProfiles` según `account.saleMode` y oculta la generación manual para cuenta completa.

### 2. `saleMode` de cuenta es mutable

**Módulo:** API

**Estado 2026-05-07:** Corregido.

**Hallazgo:** `UpdateAccountService` permite cambiar `saleMode` mediante `PUT /accounts/{id}`.

**Riesgo:** Cambiar una cuenta de `BY_PROFILE` a `FULL_ACCOUNT`, o viceversa, puede dejar perfiles y suscripciones existentes en estados incompatibles con la modalidad actual.

**Propuesta de fix:**
- Hacer `saleMode` inmutable después de la creación de la cuenta.
- Si `updates.saleMode != existing.saleMode`, devolver `BusinessRuleException`.
- Revisar si `serviceId` también debería ser inmutable cuando ya existen perfiles o suscripciones.

**Resultado:** `UpdateAccountService` rechaza cambios de `saleMode` con `BusinessRuleException` y deja de mutar ese campo durante edición. Queda pendiente discutir si `serviceId` también debe bloquearse cuando ya existen perfiles o suscripciones.

### 3. Dashboard del vendedor no carga

**Módulo:** API + Panel

**Estado 2026-05-07:** Corregido y validado.

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

**Resultado:** Los endpoints KPI respondían 200 con `content-type: application/json`, pero el cliente generado enviaba `Accept: */*` y Angular interpretaba la respuesta como `Blob`. Se declaró `produces = application/json` en `DashboardController` para corregir el contrato OpenAPI en el siguiente `api:sync`, y `MasterDashboardService` fuerza JSON con el cliente generado vigente.

### 4. Reservas y Órdenes devuelven 500 al entrar

**Módulo:** API + Panel

**Estado 2026-05-07:** Reservas y Órdenes corregidos y validados.

**Síntoma:** Al entrar a Reservas y Órdenes se reciben errores 500.

**Causa pendiente de confirmar:** Posible repetición del patrón encontrado en Clientes, Servicios, Cuentas y Suscripciones: queries con filtros opcionales nulos o contratos mal interpretados por cliente OpenAPI.

**Propuesta de investigación:**
- Capturar logs backend exactos para Reservas y Órdenes.
- Revisar repositorios JPA/native queries con parámetros opcionales.
- Revisar servicios Angular para base URL, vendor UUID, normalización de arrays y `Accept`.
- Ajustar manejo de errores para que validaciones o ownership no terminen como 500.

**Resultado parcial:** Reservas fallaba porque el panel llamaba `GET /reservations` sin prefijo `/api/v1` usando `HttpClient` manual. Se migró `ReservationsService` a `ReservationsApiService` generado, se fuerza `Accept: application/json` y `ReservationController` declara `produces = application/json`.

**Resultado final:** Órdenes fallaba en backend por JPQL con filtros opcionales nulos en `SpringDataOrderRepository.findByVendorIdFiltered(...)`. Se reemplazó por `JpaSpecificationExecutor` con predicados dinámicos en `JpaOrderAdapter`, se declaró `produces = application/json` en controllers de órdenes y se migró `OrdersService` a `OrdersApiService` generado con `Accept: application/json`.

## Prioridad Media

### 5. Contrato de acceso en detalle de suscripción

**Módulo:** API + Panel + Store

**Estado 2026-05-07:** Corregido para API + Panel vendedor. Pendiente revisar Store.

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

**Resultado:** `SubscriptionDetailResponse` ahora incluye `access.accountEmail`, `access.accountPassword`, `access.profileName`, `access.profilePin` y `access.saleMode`. El panel vendedor renderiza ese bloque en el detalle de suscripción y fuerza `Accept: application/json` al consultar el detalle.

### 6. Revisar `GET /clients/me/accesses` en frontend cliente

**Módulo:** Store / API Client

**Estado 2026-05-07:** Corregido.

**Hallazgo:** El cliente OpenAPI generado para `getMyAccesses()` usa `Accept: */*` por defecto.

**Riesgo:** Puede repetirse el bug donde Angular recibe JSON como `Blob` y el pipeline entrega `{}` o datos no iterables.

**Propuesta de fix:**
- Cuando se implemente o revise el panel cliente, forzar `Accept: application/json`.
- Normalizar respuesta a array antes de renderizar.
- Validar estados: `ACTIVE` devuelve credenciales; `SUSPENDED` devuelve credenciales `null`.

**Resultado:** Primero se mitigó forzando `Accept: application/json`; después de corregir contratos backend y ejecutar `pnpm api:sync`, el cliente generado ya usa `application/json` para `getMyAccesses()`. Se retiró el override defensivo y se conservó la normalización a arreglo como protección ante cambios de forma de respuesta.

### 7. Campo `services.details` JSON/JSONB

**Módulo:** API + DB + Docs

**Estado 2026-05-07:** Investigado. No requiere cambio inmediato.

**Hallazgo:** La tabla `services` tiene un campo `details` de tipo JSON/JSONB. Falta confirmar por qué se definió y si se usa actualmente.

**Propuesta de investigación:**
- Revisar migraciones, entidad `ServiceEntity`, DTOs y mappers.
- Verificar si `details` está expuesto en OpenAPI.
- Decidir si se conserva como metadata flexible o si debe eliminarse/documentarse.

**Resultado:** `details` nació en `V1__init_unified_schema.sql` para metadata flexible del catálogo. En `V11__normalize_services_pricing.sql` se normalizaron `price_profile`, `price_full`, `duration_days`, `description`, `image_url` e `is_active`, y se dejó explícitamente `details` solo para metadata no estructurada. El campo sigue vivo en `ServiceEntity`, dominio, persistence mapper, `ServiceRequest`, `ServiceResponse` y `ServiceMapper`, pero el formulario del panel no lo expone ni las reglas actuales lo consumen.

**Decisión técnica recomendada:** Conservarlo como campo técnico opcional para metadata futura, pero no usarlo para pricing, duración, disponibilidad, imágenes ni reglas de inventario. Si se expone en UI más adelante, debe ser un editor avanzado de JSON y no un campo operativo normal.

### 8. Imagen de servicio como upload real

**Módulo:** Panel + Supabase Storage + API

**Estado 2026-05-07:** Investigado. Pendiente decisión de Storage antes de implementar.

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

**Resultado de investigación:** El panel ya tiene `SupabaseService` con cliente Supabase disponible, pero no existe wrapper de Storage ni bucket/política documentada. El formulario de servicios solo maneja `imageUrl` manual y el backend ya persiste ese campo; por tanto, el backend no necesita cambio si la subida ocurre en el panel y se guarda la URL resultante.

**Diseño recomendado:** Crear un `ServiceImageUploadService` en el panel que valide archivo (`image/png`, `image/jpeg`, `image/webp`), tamaño máximo y nombre estable por vendor/servicio, suba a Supabase Storage y devuelva una URL. Luego `ServiceFormComponent` reemplaza el input manual por selector de archivo con preview y sigue enviando `imageUrl` al backend.

**BLOCKER:** falta definir bucket y política:
- Opción recomendada para logos de catálogo: bucket público `service-images`, porque las imágenes ya se muestran públicamente en Store.
- Alternativa: bucket privado con signed URLs, pero requiere expiración/refresh y complica el catálogo público.

No se debe implementar hasta confirmar bucket, política y límite de tamaño.

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

**Estado 2026-05-07:** Corregido tras `pnpm api:sync`.

**Hallazgo:** El cliente Angular generado por OpenAPI selecciona `responseType` según el header `Accept`. Cuando el contrato generado solo declara `*/*`, Angular puede tratar respuestas JSON como `Blob`.

**Riesgo:** Listados vacíos, objetos no iterables o respuestas `{}` aunque el backend responda 200 con JSON válido.

**Propuesta:**
- En wrappers Angular, siempre pasar `{ httpHeaderAccept: 'application/json' }` para endpoints que devuelvan JSON.
- Normalizar arrays cuando el backend pueda devolver lista directa o página `{ content: [] }`.
- Revisar si la causa raíz está en la configuración OpenAPI del backend: produces/content type debe declarar `application/json`.

**Criterio final deseado:** Los overrides `jsonResponseOptions` son deuda temporal, no la solución definitiva. La corrección raíz debe ser declarar `produces = application/json` en los endpoints JSON del backend, reconstruir el backend, ejecutar `pnpm api:sync` y verificar que el cliente generado acepte `application/json` sin castear a `'*/*'`. Después de eso se deben retirar los overrides defensivos donde ya no sean necesarios.

**Resultado:** Se declaró `produces = application/json` en los controllers JSON restantes: Auth, Accounts, Services, Profiles, Clients, Subscriptions, Assignments, Notifications y VendorPublic. Ya estaban corregidos Dashboard, Reservas y Órdenes. Después de reconstruir backend y ejecutar `pnpm api:sync`, `packages/api-client` ya genera `httpHeaderAccept?: 'application/json'` y no quedan firmas `httpHeaderAccept?: '*/*'`. Se retiraron los overrides defensivos `jsonResponseOptions` de Panel y Store.

## Orden sugerido para mañana

1. Dashboard: capturar logs y corregir causa raíz.
2. Reservas y Órdenes: confirmar si repiten patrón de queries opcionales nulas.
3. Investigar `services.details`.
4. Diseñar upload de imágenes a Supabase Storage.
5. Revisar manejo de excepciones/códigos HTTP para evitar 500 en errores esperados.
