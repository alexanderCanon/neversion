# AGENTS.md - Angular Admin Mix

This document provides guidelines for agents working on this Angular 17+ project.

## Project Overview

- **Framework**: Angular 17.3 with standalone components
- **State Management**: Angular Signals + RxJS
- **Package Manager**: pnpm
- **Backend**: Spring Boot (REST APIs) + Supabase (Authentication)
- **Styling**: SCSS + Bootstrap 5 + Bootstrap Icons
- **Testing**: Jasmine + Karma

## Agent Role & Responsibilities

- **Role:** You act as an expert in Web Layout, UX, and Angular Components.
- **Responsibility:** Focus on creating the visual interface, forms, and HTML/CSS structure.
- **Constraints:** **DO NOT** implement complex backend business logic or build custom authentication constructs; rely on the existing Spring Boot APIs and Supabase JWT tokens.

---

## Build Commands

```bash
# Start development server
pnpm start                    # or: ng serve

# Build for production
pnpm run build                # or: ng build

# Build in watch mode (development)
pnpm run watch               # or: ng build --watch --configuration development

# Run tests
pnpm test                    # or: ng test
```

### Running a Single Test

To run a single test file, edit `angular.json` and modify the test configuration to include a specific file:

```bash
# Option 1: Using karma.conf.js (create one if needed)
ng test --include="**/auth.service.spec.ts"

# Option 2: Directly with karma (if karma.conf.js exists)
karma start karma.conf.js --single-run --include="**/supabase.service.spec.ts"
```

**Recommended approach**: Create a `karma.conf.js` for single-test runs:

```javascript
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {},
      clearContext: false
    },
    jasmineHtmlReporter: {
      suppressAll: true
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/angular-admix'),
      subdir: '.',
      reporters: [{ type: 'html' }, { type: 'text-summary' }]
    },
    reporters: ['progress', 'kjhtml'],
    browsers: ['Chrome'],
    restartOnFileChange: true
  });
};
```

Then run: `karma start karma.conf.js --single-run --include="**/filename.spec.ts"`

---

## Code Style Guidelines

### General Principles

- Follow Angular best practices and style guide
- Use strict TypeScript mode (enabled in `tsconfig.json`)
- Prefer standalone components over NgModules
- Use signals for local component state, services for shared state
- **Forms**: Use **Reactive Forms** exclusively (`FormBuilder`, `Validators`, `FormGroup`). Template-driven forms should be avoided.
- **Control Flow**: Prefer the new Angular 17 control flow (`@if`, `@for`, `@defer`) over legacy structural directives (`*ngIf`, `*ngFor`). Display validation errors visually using this new syntax.

### Feature-Based Architecture

This project uses a feature-based architecture with two main directories:

```
src/app/
├── core/                    # Shared/core functionality
│   ├── guards/              # Route guards (auth.guard.ts, guest.guard.ts)
│   └── services/           # Singleton services (auth.service.ts, supabase.service.ts)
├── features/               # Feature modules (domain-specific)
│   ├── login/              # Login feature
│   ├── dashboard/          # Dashboard feature
│   └── products/           # Products feature
├── app.component.ts        # Root component
├── app.config.ts           # App configuration (providers)
└── app.routes.ts           # Route definitions
```

#### Core Layer (`core/`)

Contains shared services and guards that are used across multiple features:

- **Services**: Place in `core/services/` - use `@Injectable({ providedIn: 'root' })` for singletons
- **Guards**: Place in `core/guards/` - use functional guards (`CanActivateFn`)
- **Interfaces/Types**: Place in `core/` or alongside the service that uses them

```typescript
// Core service example
@Injectable({ providedIn: 'root' })
export class AuthService { }

// Core guard example
export const authGuard: CanActivateFn = (route, state) => { ... };
```

#### Features Layer (`features/`)

Each feature is a self-contained directory with its own components, services, and types:

- **Feature components**: Standalone components specific to the feature
- **Feature-specific services**: Services only used by that feature (can be in the feature folder)
- **Feature routes**: Defined in `app.routes.ts` using lazy loading

```typescript
// app.routes.ts - lazy load features
{
  path: 'dashboard',
  loadComponent: () => import('./features/dashboard/dashboard.component')
    .then(m => m.DashboardComponent),
  canActivate: [authGuard]
}
```

#### Adding a New Feature

1. Create feature directory: `src/app/features/<feature-name>/`
2. Create component(s) in the feature directory
3. Add route in `app.routes.ts` with lazy loading
4. Add guard if the route requires authentication

```bash
# Example: creating a new 'users' feature
src/app/features/users/
├── users.component.ts      # Main users component
├── users.component.html
├── users.component.scss
└── users.component.spec.ts
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Components | kebab-case | `products.component.ts` |
| Component Class | PascalCase | `ProductsComponent` |
| Services | kebab-case + .service | `supabase.service.ts` |
| Service Class | PascalCase | `SupabaseService` |
| Guards | kebab-case | `auth.guard.ts` |
| Interfaces | PascalCase | `AuthResult` |
| Variables | camelCase | `currentUser` |
| Constants | camelCase or UPPER_SNAKE | `MAX_RETRIES` |
| Private properties | Prefix with `_` | `_currentUser` |

### Imports

- Use absolute imports for app modules: `@app/core/services/auth.service`
- Use relative imports for sibling components: `./../services/auth.service`
- Group imports in this order:
  1. Angular core imports
  3. Third-party libraries
  4. App imports
  5. Relative app imports

```typescript
// Good import order
import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { AuthService } from '@app/core/services/auth.service';
import { User } from '@supabase/supabase-js';
```

### TypeScript & Typing

- Always enable strict mode (`strict: true` in `tsconfig.json`)
- Use explicit types; avoid `any`
- Use interfaces for object shapes; use types for unions/intersections
- Use `unknown` when type is truly unknown, then narrow with type guards

```typescript
// Good
interface User {
  id: string;
  email: string;
}

private handleError(err: unknown): string {
  return err instanceof Error ? err.message : 'Unknown error';
}

// Avoid
// private handleError(err: any): string { ... }
```

### Component Structure

Use this order in component files:

```typescript
// 1. Imports (Angular, third-party, app)
// 2. Interfaces/types (if component-specific)
// 3. Component decorator
// 4. Component class
//   - Private readonly fields (injected services)
//   - Public fields (inputs, outputs)
//   - Constructor or field initializers (signals)
//   - Lifecycle hooks
//   - Public methods
//   - Private methods
```

### Signals

- Use `signal()` for writable state
- Use `computed()` for derived state
- Expose signals as readonly using `.asReadonly()`
- Prefix private signal names with `_`

```typescript
private readonly _currentUser = signal<User | null>(null);
readonly currentUser = this._currentUser.asReadonly();
readonly isAuthenticated = computed(() => this._currentUser() !== null);
```

### Error Handling

- Always handle errors in Observable pipes using `catchError`
- Use `finalize` for cleanup (loading states, etc.)
- Provide user-friendly error messages
- Use type guards when handling unknown error types

```typescript
return from(promise).pipe(
  map((response) => this.handleAuthResponse(response)),
  catchError((err: unknown) => {
    const message = err instanceof Error ? err.message : 'Unexpected error';
    return of({ success: false, error: message });
  }),
  finalize(() => this._isLoading.set(false)),
);
```

### Guards

Use functional guards (`CanActivateFn`) instead of class-based:

```typescript
export const authGuard: CanActivateFn = async (route, state) => {
  const router = inject(Router);
  const supabaseService = inject(SupabaseService);

  const { data } = await supabaseService.client.auth.getSession();

  if (data.session) return true;

  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url }
  });
};
```

### Routing

- Use lazy loading with `loadComponent`
- Define routes in `app.routes.ts`
- Use guards for authentication

```typescript
{
  path: 'dashboard',
  loadComponent: () => import('./pages/dashboard/dashboard.component')
    .then(m => m.DashboardComponent),
  canActivate: [authGuard]
}
```

### SCSS / Layout & Styling (Strict Rules)

- **Color Palette:** Rely on defined OKLCH CSS variables in `:root` of `src/styles.scss` (e.g., `var(--primary-color)`) before hardcoding colors. The default font is Inter.
- **Bootstrap First:** ALWAYS prioritize utility classes (`d-flex`, `p-3`, `gap-2`) and the Grid System (`container`, `row`, `col`) over custom CSS.
- **Visual Style:** Keep design sober and modern. Use `shadow-sm` or subtle borders (avoid `shadow-lg`). Use `rounded` or `rounded-3` (no `rounded-pill` except for badges).
- **UI Patterns:**
  - **Cards:** Main containers should use `<div class="card shadow-sm border-0">`.
  - **Forms:** Apply `form-control` to inputs with a clear `form-label`.
  - **Buttons:** Use `btn btn-primary` for primary actions, `btn btn-success` for positive/save actions, `btn btn-outline-secondary` for cancel/back, and `routerLink` for secondary navigation.
  - **Animations:** Limit to simple CSS transitions (`transition: 0.2s ease-in-out`).
- Keep component styles in separate `.scss` files (`styleUrl`, not `styleUrls`) and only use custom CSS when utility classes are insufficient.
- Follow BEM naming for custom CSS classes when needed.

### Testing

- Place tests alongside components with `.spec.ts` extension
- Use `TestBed.configureTestingModule` for integration tests
- Use `standalone: true` components directly in `imports` array
- Mock services using Jasmine spies or TestBed providers

```typescript
describe('AuthService', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [AuthService, /* mock providers */]
    }).compileComponents();
  });
});
```

---

## Configuration Files

| File | Purpose |
|------|---------|
| `angular.json` | Angular CLI configuration |
| `tsconfig.json` | TypeScript compiler options (strict mode enabled) |
| `tsconfig.app.json` | App-specific TypeScript config |
| `tsconfig.spec.json` | Test-specific TypeScript config |
| `package.json` | Dependencies and npm scripts |
| `src/environments/environment.ts` | Environment variables (Supabase URL/Key) |

---

## Key Dependencies

- `@angular/core`: ^17.3.0
- `@supabase/supabase-js`: ^2.95.3
- `bootstrap`: ^5.3.8
- `bootstrap-icons`: ^1.13.1
- `rxjs`: ~7.8.0
- `typescript`: ~5.4.2
- `jasmine-core`: ~5.1.0
- `karma`: ~6.4.0
