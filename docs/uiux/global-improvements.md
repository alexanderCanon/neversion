# Global Improvements — Cross-cutting

> These issues affect the entire panel and must be resolved before module-specific improvements.
> They form the foundation of a unified design system.

---

## 1. Language Standardization

**Status:** Mixed — Spanish in Products module, English in Accounts, Clients, and Subscriptions.

**Rule:** All user-facing text must be in Spanish. Internal code values (enums, DB constants) stay in English.

**Translation map:**

| Current (EN) | Proposed (ES) | Module |
|---|---|---|
| Accounts | Cuentas | Accounts |
| New Account | Nueva cuenta | Accounts |
| AVAILABLE / ASSIGNED | DISPONIBLE / ASIGNADO | Accounts |
| Deactivate | Desactivar | Accounts |
| Users | Clientes | Clients |
| New User | Nuevo cliente | Clients |
| Create User | Registrar cliente | Clients |
| Actions | Acciones | Global |
| Subscriptions | Suscripciones | Subscriptions |
| New Subscription | Nueva suscripción | Subscriptions |
| All Statuses | Todos los estados | Subscriptions |
| No subscriptions found | Sin suscripciones registradas | Subscriptions |
| Refresh | Actualizar | Global |
| Profile Name | Nombre de perfil | Accounts |
| Status | Estado | Global |

**Technical note:** DB-stored enum values (`AVAILABLE`, `ASSIGNED`, `STREAMING`) must NOT be renamed.
Translate only at the presentation layer using a pipe or utility function in Angular.

```typescript
// Recommended: translation pipe
// shared/pipes/status-label.pipe.ts
const STATUS_LABELS: Record<string, string> = {
  AVAILABLE: 'Disponible',
  ASSIGNED: 'Asignado',
  ACTIVE: 'Activo',
  EXPIRED: 'Vencido',
  CANCELLED: 'Cancelado',
  SUSPENDED: 'Suspendido',
};
```

---

## 2. Typography System

**Status:** At least 3 different capitalization conventions coexist. No documented type scale.

**Proposed scale:**

| Element | Size | Weight | Color |
|---|---|---|---|
| Page title (H1) | 24px | Bold | `#1F2937` |
| Section subtitle (H2) | 18px | SemiBold | `#1E40AF` |
| Form label | 13px | Medium | `#6B7280` |
| Table / body text | 14px | Regular | `#1F2937` |
| Placeholder / secondary | 13px | Regular | `#9CA3AF` |
| Status badge | 11px | Bold | uppercase + letter-spacing |

**Capitalization rules:**

- Module titles -> Sentence case: `Administración de productos` (Correct) / `ADMINISTRACIÓN DE PRODUCTOS` (Incorrect)
- Form labels -> Sentence case: `Nombre del producto` (Correct) / `NOMBRE DEL PRODUCTO` (Incorrect)
- Buttons -> Sentence case: `Nueva cuenta` (Correct) / `NEW ACCOUNT` (Incorrect)
- Status badges -> UPPERCASE is incorrect: `ACTIVO`, `DISPONIBLE` (Incorrect), instead use SENTENCECASE.

---

## 3. Color System & Design Tokens

**Status:** At least 3 different primary button colors across modules (black, blue, green).
Dashboard metric cards use 5 colors with no semantic meaning.

**Proposed CSS tokens** (add to `src/styles.scss`):

```scss
:root {
  --color-primary: #1E40AF;       // Primary actions, active nav
  --color-primary-hover: #1D4ED8;
  --color-danger: #DC2626;        // Delete, deactivate
  --color-success: #16A34A;       // Active state, confirmation
  --color-warning: #D97706;       // Expiring soon, partial
  --color-neutral: #6B7280;       // Cancel, secondary actions
  --color-surface: #F9FAFB;       // Page background
  --color-border: #E5E7EB;
  --color-text-primary: #1F2937;
  --color-text-secondary: #6B7280;
  --color-text-placeholder: #9CA3AF;
}
```

**Button rules:**

| Action type | Style |
|---|---|
| Primary (create, save, continue) | `background: var(--color-primary)` |
| Destructive (delete, deactivate) | `background: var(--color-danger)` |
| Neutral (cancel, back) | `border + color: var(--color-neutral)`, no background |

**Dashboard metric card colors** must follow semantic meaning:
- Total accounts, slots -> `--color-primary`
- Active subscriptions -> `--color-success`
- Expiring soon -> `--color-warning`
- No meaning / neutral count -> `--color-neutral`

---

## 4. Dark / Light Mode

**Status:** Not implemented. The sidebar already uses a dark scheme (`#1F2937`), which provides a visual foundation.

**Implementation approach (Angular):**

```typescript
// core/services/theme.service.ts
toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme');
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('theme', next);
}
```

```scss
// styles.scss
[data-theme='dark'] {
  --color-surface: #111827;
  --color-text-primary: #F9FAFB;
  // ...override tokens
}
```

- Persist preference in `localStorage`
- Respect `prefers-color-scheme` as initial default
- Toggle icon (sun/moon) in the top navbar

---

## 5. Regional Format (Guatemala)

**Status:** Currency shows `$`, phone placeholder is `+1234567890`, some dates use ISO format.

**Required adjustments:**

| Format | Current | Correct |
|---|---|---|
| Currency | `$95` / `Q1250` | `Q 1,250.00` |
| Phone placeholder | `+1234567890` | `+502 ####-####` |
| Dates in tables | `2026-04-05` (ISO) | `05/04/2026` (dd/mm/yyyy) |
| Dates in forms | `dd/mm/aaaa` | Keep — already correct |

**Note:** The field currently labeled `Price (Seller)` represents the **cost paid to the provider** (not the price charged to the client). Rename to `Precio de compra (proveedor)` and add a tooltip for clarity.

---

## 6. Filters Without Logic

**Status:** Filter controls exist in the UI but do nothing. This creates false affordance — the user interacts expecting a result and gets none.

**Affected controls:**
- "Todas las Categorías" dropdown -> Products module
- "All Statuses" dropdown -> Subscriptions module
- Search bar -> Accounts and Clients modules

**Proposed priority:**

| Filter | Priority | Notes |
|---|---|---|
| Subscription status filter | High | Core operational need (Active, Expired, Expiring soon) |
| Client search (name, email, phone) | High | Will be needed as client list grows |
| Product category filter | Medium | Less urgent with few products |

**While unimplemented:** disable the controls visually and add a tooltip `"Próximamente"` or `"Coming soon"` to avoid false expectations.

---

## 7. Loading States

**Status:** Components render the empty state before the HTTP response arrives, causing flickers:
- `"No users"` flash before clients load
- `"Loading Data"` visible text in Subscriptions modal

**Rule for all modules:** implement 4 explicit states:

```typescript
type ComponentState = 'loading' | 'loaded' | 'empty' | 'error';
```

- Show skeleton/spinner during `loading`
- Show data during `loaded`
- Show empty state ONLY when `loaded` AND data is empty
- Show error message when `error`

Never show the empty state while loading.

---

## 8. Angular Template Structure

**Status:** HTML templates found inline inside `.ts` files in some components.

**Rule:** All components must use `templateUrl` pointing to a separate `.html` file.

```typescript
// Incorrect (Avoid)
@Component({
  template: `<div>...</div>`
})

// Correct
@Component({
  templateUrl: './my-component.component.html',
  styleUrl: './my-component.component.scss'
})
```

This also applies to `styleUrls` -> use `styleUrl` (singular, Angular 17).
