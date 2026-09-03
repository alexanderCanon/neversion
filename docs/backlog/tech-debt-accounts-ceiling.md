# Refactor — Techo de Servicios y borrado de Perfil

**Decisión Alex (2026-09-03):** Opción A — el `maxProfiles` del Servicio es techo absoluto.
**Rama:** `feat/account-ceiling-and-profile-delete`
**Alcance:** `apps/panel` (forms de cuenta, lista de perfiles) + backend `~/projects/neversion-api` (crear/editar cuenta).

---

## Friction 1 — Sin techo (resuelto en esta rama)

**Problema:** al crear/editar una Cuenta se podía poner `maxProfiles` mayor que el máximo del Servicio (el panel solo valida mínimo 1 y el backend acepta cualquier valor > 0).

**Enforcement:**
- Backend `CreateAccountService` / `UpdateAccountService`: rechazar con `400 BusinessRuleException` si lo pedido supera `service.maxProfiles`.
- Panel `account-form` + `account-with-subscription-form`: `Validators.max = service.maxProfiles`, hint y ajuste al cambiar de servicio.

## Friction 2 — Sin botón para borrar Perfil (resuelto en esta rama)

**Problema:** tras la autogeneración, bajar slots obliga a borrar perfiles, pero el panel no tenía botón (solo existe `DELETE /profiles/{id}` en API).

**Solución:** botón eliminar en `profile-list`, solo en perfiles `AVAILABLE` sin suscripción activa, con confirmación y refresco.

---

## Mini-tareas

- [ ] 1. Auditoría de cuentas sobre el techo — **pendiente de Alex**: correr en DB `SELECT email, max_profiles FROM accounts …` (ver query abajo) y decidir legacy.
- [ ] 2. Enforcement backend crear/editar + tests (tests los corre Alex fuera del sandbox).
- [ ] 3. Tope en los 2 forms del panel.
- [ ] 4. Botón eliminar Perfil disponible en `profile-list`.
- [x] 5. Documentar (BR-02 + este archivo).

## Query de auditoría (Alex)

```sql
SELECT a.email, a.max_profiles AS account_max, s.name AS service, s.max_profiles AS service_max
FROM accounts a JOIN services s ON s.id = a.service_id
WHERE a.max_profiles > s.max_profiles;
```

Cuentas legacy que salgan aquí se respetan tal cual; el candado aplica solo a crear/editar desde esta rama en adelante.
