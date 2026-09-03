# Tech Debt — Renovación selectiva por grupo

**Origen:** refactor directo del botón "Renovar todas" (sesión 2026-09-03, sin diseño previo).
**Ramas:** `feat/account-ceiling-and-profile-delete` (panel) + `feat/account-maxprofiles-ceiling` (backend).
**Estado:** funcional y commiteado; pendiente pulir en próxima sesión.

## TD-R1 — Gracia duplicada panel/backend

El panel trae los 2 días de gracia hardcodeados (`renewGraceDays` en `subscriptions-list.component.ts`)
para el badge "fuera de gracia" y el input de fecha explícita. El backend lo tiene configurable
(`neversion.renewal.grace-period-days`, default 2). Si cambia allá, el panel miente.
**Arreglo:** exponer la gracia (endpoint de config o incluirla en respuestas) y que el panel la consuma.

## TD-R2 — Fecha explícita sin techo

`PUT /subscriptions/{id}/renew?newDueDate=` solo rechaza fechas pasadas. Acepta cualquier futuro,
por lejano que sea. Decidir si se topa (ej. máx. +60/90 días) o se deja a criterio del vendedor.

## TD-R3 — Camino paralelo al api-client ✅ cerrado 2026-09-03

~~El api-client publicado 1.0.0 no expone `newDueDate`...~~ Cliente 1.0.1 publicado con el parámetro;
panel migrado a `renewSubscription(id, newDueDate)` tipado y eliminado el `HttpClient` manual.

## TD-R4 — Default hoy+30 no preserva el día

El input de fecha explícita defaultea a hoy+30. Para el caso "mantener el día 16" hay que elegirla
a mano. Evaluar default inteligente (mismo número de día del mes siguiente) o dejarlo manual.
