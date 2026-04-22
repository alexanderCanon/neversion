# Neversion — API Conventions

> Consolidación de los principios de diseño API, estándares de errores HTTP y flujo de desarrollo. Fuente de verdad para todo el backend.

---

## 1. Principios Base

- **Contract-First Development:** La API es el producto; el código es la implementación. Ningún código debe escribirse antes de definir los JSON de request/response en los archivos de módulo.
- **Markdown como fuente de verdad:** Los archivos `.md` de módulos en `docs/modules/` dictan la implementación del backend.
- **Consistencia:** Todos los controllers deben compartir estructura uniforme de respuestas, verbos HTTP consistentes y lógica de seguridad centralizada.

---

## 2. Diseño de API y Routing

### Versionado

Todas las APIs usan versionado por URI:

```
/api/v1/{module}
```

- **Regla:** Cambios rompe-compatibilidad (cambio de tipos o eliminación de campos) requieren bump a `/v2`.

### REST Semántico

```json
// ✅ CORRECTO — respuesta directa del recurso
{
  "orderId": "123",
  "status": "PENDING"
}

// ❌ EVITAR — wrapper genérico (rompe semántica HTTP pura)
{
  "success": true,
  "data": { "orderId": "123" }
}
```

### Verbos HTTP

| Verbo | Uso |
|---|---|
| `GET` | Consultas (sin efectos secundarios) |
| `POST` | Creación de recursos |
| `PUT` | Actualización completa / acción de estado |
| `DELETE` | Eliminación (soft o hard según el módulo) |

---

## 3. Manejo de Errores — RFC 7807 (Problem Details)

Todos los errores implementan el estándar **RFC 7807**. Garantiza consistencia entre panel frontend y API backend.

### Estructura base

```json
{
  "type": "https://api.neversion.com/errors/bad-request",
  "title": "Bad Request",
  "status": 400,
  "detail": "Data validation has failed.",
  "instance": "/api/v1/orders/123"
}
```

| Campo | Descripción |
|---|---|
| `type` | URI que identifica el tipo de problema |
| `title` | Resumen legible en texto |
| `status` | Código HTTP (debe coincidir con el header) |
| `detail` | Explicación específica de este error |
| `instance` | URI del recurso que causó el error |

### Extensión — Errores de validación de campos

```json
{
  "type": "https://api.neversion.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "There are errors in the submitted fields.",
  "instance": "/api/v1/orders",
  "fieldErrors": [
    {
      "field": "price",
      "message": "The price cannot be negative",
      "rejectedValue": -50
    }
  ]
}
```

---

## 4. Códigos HTTP — Guía de Uso

### Respuestas exitosas

| Código | Cuándo usar |
|---|---|
| `200 OK` | Consultas (`GET`), actualizaciones (`PUT`/`PATCH`), acciones exitosas |
| `201 Created` | Creación de recurso (`POST`). Idealmente retornar el recurso creado |
| `204 No Content` | Eliminaciones exitosas (`DELETE`) sin cuerpo de respuesta |

### Errores de cliente (4xx)

| Código | Cuándo usar |
|---|---|
| `400 Bad Request` | Input inválido, JSON malformado, tipos incorrectos, validaciones `@Valid` |
| `401 Unauthorized` | Usuario no autenticado, JWT ausente, expirado o inválido |
| `403 Forbidden` | Autenticado pero sin permisos (roles insuficientes) |
| `404 Not Found` | Recurso no existe (`ResourceNotFoundException`) |
| `409 Conflict` | **Violaciones de reglas de negocio** (`BusinessRuleException`) o integridad referencial (`DataIntegrityViolationException`) |

### Errores de servidor (5xx)

| Código | Cuándo usar |
|---|---|
| `500 Internal Server Error` | Errores no controlados. **No exponer StackTraces al cliente.** Solo "Internal server error". |

---

## 5. Flujo de Desarrollo

1. **Diseñar el contrato:** Escribir los payloads JSON esperados, modelos DTO y lógica HTTP en el módulo correspondiente en `docs/modules/`.
2. **Revisar:** Asegurar que los endpoints alinean con las reglas de negocio en `docs/modules/`.
3. **Implementar:** Escribir los controllers y DTOs de Spring Boot mapeados exactamente al contrato.
4. **Probar:** Crear Integration Tests (`*IT.java`) que cubran esos límites exactos.
5. **Deploy:** Containerizar y publicar.

> *"Si el contrato está mal, el sistema está mal."*

---

## 6. Deuda Técnica Conocida

> [!NOTE]
> **Estandarización del campo `type` (RFC 7807):**
> La implementación actual retorna strings simples para identificar errores (ej. `"Conflict"`, `"Bad Request"`) en lugar de URIs canónicas completas. En el futuro se estandarizará el uso de un dominio real y resolvible (ej. `https://api.neversion.com/errors/business-rule-violation`) para cumplir estrictamente el formato de URL requerido por RFC 7807.

---

## Cuándo leer este archivo

- Antes de implementar cualquier controller o endpoint nuevo
- Para saber cómo manejar errores de validación vs. errores de negocio
- Para revisar el estándar de respuestas HTTP del sistema
