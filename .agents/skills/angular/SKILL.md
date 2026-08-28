---
name: angular
description: Canonical Angular skill for Neversion. Use for Angular 21 (`apps/panel`) development, refactors, testing, routing, forms, accessibility, and CLI workflows.
---

# Angular for Neversion

Use this skill for all Angular work in this repo. It is the only Angular skill agents should load here.

## Version scope

- `apps/panel` uses Angular 21 with standalone components and signals.

## Defaults

- Follow the existing style of the target app before introducing new patterns.
- Use `inject()` when the codebase already follows it.
- Use standalone components in `apps/panel`.
- Keep code and comments in English.
- Keep UI labels and visible copy in Spanish.
- Prefer interfaces and typed models over `any`.
- Use signals only where they fit the app and current architecture.
- Use reactive forms for complex forms; keep template-driven forms only where the existing code already uses them.
- Avoid unrelated refactors or abstractions.

## Component and template rules

- Use semantic HTML and ARIA where it matters.
- Associate every `label` with `for` and `id`.
- Add keyboard support for clickable non-button elements.
- Use `async` pipe for observable consumption in templates.
- Use `trackBy` or the Angular 17 `track` clause for repeated lists.
- Prefer SSR-safe browser access when touching `window`, `document`, or third-party JS.

## State and routing

- Use RxJS and services for server state.
- Use signals for local UI state in Angular 17 when the app already follows that pattern.
- Keep route state explicit and lazy-load feature routes where the app already does.
- Use guards and interceptors consistent with the target app.

## Bootstrap and UI integration

- Keep Bootstrap interactions SSR-safe.
- Prefer typed wrappers over `any` when opening or closing modals.
- Do not add visual patterns that fight the existing Bootstrap-based UI.

## Testing and verification

- Add or update focused specs when behavior changes.
- Verify changed Angular code with the repo's existing build or test command when validation is needed.

## Scaffolding

- Do not use a separate Angular new-app skill in this repo.
- Use the repo's own docs and existing app structure for Angular work.
