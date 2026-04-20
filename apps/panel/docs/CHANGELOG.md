# Changelog - Panel Application

## [2026-04-20] - Lint Refactoring and Angular 17 Standards

Fixed 55 lint errors across multiple files to align with Angular 17 standards and project-specific rules.

### Added
- `Bootstrap` and `BootstrapModal` interfaces in components using Bootstrap's JavaScript API to ensure type safety without `any`.
- Accessibility attributes (`role="button"`, `tabindex`, `aria-expanded`) to clickable elements.
- Keyboard event handlers (`keydown.enter`, `keydown.space`) to non-interactive elements with click events.
- `display: table-row` to `:host` style in `ProfileRowComponent` to maintain table structure after changing selector to element.

### Changed
- Refactored all constructor-based dependency injections to use the `inject()` function (`@angular-eslint/prefer-inject`).
- Changed `ProfileRowComponent` selector from attribute `[app-profile-row]` to element `app-profile-row`.
- Renamed output event `toggle` to `accountToggle` in `AccountRowComponent` to avoid conflict with native DOM `toggle` event.
- Updated `ProductAccountsPageComponent` to match the new `accountToggle` event and `app-profile-row` element usage.
- Improved type safety by replacing `any` with `unknown` or specific interfaces (e.g., `ServiceDetails`) in models and services.
- Associated `<label>` elements with their corresponding form controls using `id` and `for` attributes.

### Removed
- Unused imports and variables identified by `@typescript-eslint/no-unused-vars`.
- Empty lifecycle methods (`ngOnInit`, `ngOnChanges`) and empty functions.
- Redundant `as any` type casts in service calls.

### Files Modified
- `src/app/core/layout/main-layout/main-layout.component.ts`
- `src/app/core/services/auth.service.ts`
- `src/app/features/accounts/components/account-form/account-form.component.html`
- `src/app/features/accounts/components/account-form/account-form.component.ts`
- `src/app/features/accounts/components/profile-list/profile-list.component.ts`
- `src/app/features/accounts/pages/accounts-list/accounts-list.component.ts`
- `src/app/features/clients/components/client-form/client-form.component.ts`
- `src/app/features/dashboard/components/account-row/account-row.component.ts`
- `src/app/features/dashboard/components/profile-row/profile-row.component.ts`
- `src/app/features/dashboard/pages/product-accounts-page.component.ts`
- `src/app/features/login/components/login-form/login-form.component.html`
- `src/app/features/login/login.component.ts`
- `src/app/features/orders/pages/orders-list/orders-list.component.html`
- `src/app/features/reservations/pages/reservation-detail/reservation-detail.component.html`
- `src/app/features/reservations/pages/reservations-list/reservations-list.component.html`
- `src/app/features/reservations/services/reservations.service.ts`
- `src/app/features/services/components/service-form/service-form.component.ts`
- `src/app/features/services/components/services-table/services-table.component.ts`
- `src/app/features/services/models/service.model.ts`
- `src/app/features/services/pages/services-list/services-list.component.ts`
- `src/app/features/subscriptions/components/subscription-form/subscription-form.component.ts`
- `src/app/features/subscriptions/pages/subscriptions-list/subscriptions-list.component.ts`
- `src/app/features/subscriptions/services/subscriptions.service.ts`
