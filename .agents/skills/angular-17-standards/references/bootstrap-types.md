# Bootstrap Type Safety Patterns

To eliminate `any` usage when interacting with Bootstrap's JavaScript API, use these interfaces.

## Pattern 1: Component-Specific Declaration

Place this at the top of components that need to manipulate Bootstrap modals or other elements.

```typescript
interface BootstrapModal {
  show(): void;
  hide(): void;
}

interface Bootstrap {
  Modal: {
    new (el: HTMLElement): BootstrapModal;
    getInstance(el: HTMLElement): BootstrapModal | null;
  };
}

declare const bootstrap: Bootstrap;
```

## Pattern 2: Global Window Access

Use this when accessing `bootstrap` through the `window` object in SSR-safe contexts (using `isPlatformBrowser`).

```typescript
if (isPlatformBrowser(this.platformId)) {
  const bootstrap = (window as unknown as { bootstrap: Bootstrap }).bootstrap;
  if (bootstrap) {
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }
}
```

## Application to Modals

### Showing a Modal
```typescript
const modalEl = this.modalElement?.nativeElement;
if (modalEl) {
  const modal = new bootstrap.Modal(modalEl);
  modal.show();
}
```

### Closing a Modal
```typescript
const modalEl = this.modalElement?.nativeElement;
if (modalEl) {
  const modal = bootstrap.Modal.getInstance(modalEl);
  if (modal) modal.hide();
}
```
