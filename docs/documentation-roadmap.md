# Roadmap de Documentación — Neversion

Este documento consolida las brechas, ambigüedades y observaciones detectadas en el sistema de documentación de **Neversion**. Todos los ítems han sido resueltos.

## Brechas Resueltas

### 1. ~~Gestión de Combos y Paquetes~~ → Resuelto
*   **Decisión:** No se crea tabla `COMBOS`. Los descuentos son reglas dinámicas configuradas en `discount_cfg` (JSONB) en `VENDORS`.
*   **Criterio:** Cantidad total de ítems en el carrito, incluyendo servicios repetidos.
*   **Documentado en:** BR-13 (business-rules.md), er-diagram.md.

### 2. ~~Parametrización de BR-07~~ → Resuelto
*   **Decisión:** Constante de aplicación en `application.yml`, no en base de datos.
*   **Documentado en:** BR-07 (business-rules.md), ADR-07 (decisions.md).

### 3. ~~Modelo de Permisos Granular~~ → Resuelto
*   **Decisión:** RBAC implícito por rol. Sin tabla de permisos. Guards en el backend validan `role`.
*   **Documentado en:** ADR-08 (decisions.md), gap-analysis.md.

### 4. ~~Estrategia Técnica de Migración~~ → Resuelto
*   **Decisión:** Migración manual vía panel del vendedor. Volumen <100 registros. Sin importación masiva.
*   **Documentado en:** BR-20 (business-rules.md).

---

## Ambigüedades Resueltas

### 1. ~~Lógica de "Venta por Cuenta Completa"~~ → Resuelto
*   **Decisión:** Todos los perfiles se vinculan a una sola suscripción. La cuenta pasa a estado `sold`. El cliente recibe solo credenciales de la cuenta maestra.
*   **Documentado en:** BR-21 (business-rules.md).

### 2. ~~Algoritmo de Sugerencia de Asignación~~ → Resuelto
*   **Decisión:** Priorizar cuentas con más tiempo de vigencia restante (`renewal_date` DESC). Para perfil individual: primer perfil `available` de la cuenta seleccionada.
*   **Documentado en:** BR-15 (business-rules.md).

### 3. ~~Proveedores de Infraestructura~~ → Resuelto
*   **Decisión:** Supabase Auth para autenticación, Resend para email transaccional.
*   **Documentado en:** ADR-09, ADR-10 (decisions.md), deployment.md.

---

## Observaciones Resueltas

### 1. ~~Integridad de Reglas en el Esquema~~ → Resuelto
*   **Decisión:** Las reglas de inmutabilidad se validan en la capa de dominio (Application Service), no mediante triggers de BD. Consistente con arquitectura hexagonal (NFR-04).
*   **Documentado en:** BR-05 (business-rules.md).

---

## Checklist de Roadmap

1. [x] **Definir Modelo de Combos** → `discount_cfg` JSONB con tiers.
2. [x] **Elegir Proveedores** → Supabase Auth + Resend.
3. [x] **Detallar Algoritmo de Asignación** → Vigencia restante DESC.
4. [x] **Parametrizar BR-07** → Constante de aplicación.
