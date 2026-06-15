# Tech Debt — Panel: Assignment & Subscription Forms

**Detectado:** 2026-06-15  
**Módulos afectados:** `apps/panel/src/app/features/assignments`, `apps/panel/src/app/features/subscriptions`  
**Severidad:** Media-Alta (no crítico hoy, doloroso a escala)

---

## TD-01 — Carga masiva de clientes sin paginación

**Archivos:**
- `features/assignments/components/manual-assignment-modal/manual-assignment-modal.component.ts`
- `features/subscriptions/components/subscription-form/subscription-form.component.ts`

**Problema:**  
Ambos componentes descargan la lista completa de clientes del vendor al abrir el modal (`getClients()` sin parámetros). El filtrado del input de búsqueda es 100% local en memoria via `Array.filter()`. Con volumen alto (>500 clientes) esto genera:
- Payload grande en cada apertura del modal
- Filtrado en el browser en cada keystroke

**Solución propuesta:**  
Reemplazar la carga masiva + filtro local por una búsqueda debounced contra el backend:
```ts
this.searchControl.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  filter(term => term.length >= 2),
  switchMap(term => this.clientsService.getClients({ name: term }))
).subscribe(clients => this.filteredClients = clients);
```
Requiere verificar que el endpoint `GET /clients` soporte búsqueda por `name`/`phone` como query params.

---

## TD-02 — Filtro de cuentas EXPIRED en frontend (no movible al backend)

**Archivos:**
- `features/assignments/components/manual-assignment-modal/manual-assignment-modal.component.ts`
- `features/subscriptions/components/subscription-form/subscription-form.component.ts`

**Problema:**  
El filtro `accounts.filter(a => a.status !== 'EXPIRED')` ocurre en el frontend porque el endpoint `GET /accounts` solo acepta un `status` **inclusivo** (dame las de este estado), no exclusivo (dame todas menos este). No existe un parámetro `excludeStatus` ni soporte para múltiples valores.

**Solución propuesta (backend):**  
Agregar soporte de exclusión en el endpoint del backend, por ejemplo con un parámetro `excludeStatus=EXPIRED`, o aceptar múltiples valores de status separados por coma. Esto requiere cambio en `AccountController` y `SubscriptionSecurityConfig`.

**Mientras tanto:** el filtro en frontend es aceptable dado que el `serviceId` ya reduce el conjunto considerablemente.

---

## TD-03 — Lógica de carga duplicada entre dos componentes (DRY violation)

**Archivos:**
- `features/assignments/components/manual-assignment-modal/manual-assignment-modal.component.ts`
- `features/subscriptions/components/subscription-form/subscription-form.component.ts`

**Problema:**  
Ambos componentes implementan la misma lógica de ~150 líneas:
- Cargar clientes al abrir
- Cargar servicios al abrir
- Cascada `serviceId → cuentas → perfiles`
- Búsqueda/filtrado de clientes

Un bug en uno implica revisión y fix en el otro (como se demostró en esta sesión).

**Solución propuesta:**  
Extraer a un servicio o clase base compartida, por ejemplo `AssignmentFormStateService`, que encapsule:
- `loadDropdownData()`
- `loadAccountsForService(serviceId)`
- `loadProfilesForAccount(accountId)`
- Streams de `filteredClients$`, `accounts$`, `profiles$`

Ambos componentes lo inyectarían y solo diferirían en su payload de submit.
