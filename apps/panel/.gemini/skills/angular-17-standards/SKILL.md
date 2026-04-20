---
name: angular-17-standards
description: Refactor and maintain Angular 17 projects following modern standards. Use when you need to refactor constructor injection to `inject()`, fix lint errors related to accessibility, type safety, or unused code, or implement standalone components with element-based selectors and Bootstrap JavaScript integrations.
---

# Angular 17 Standards Skill

This skill provides expert guidance and reusable patterns for maintaining modern Angular 17 applications, ensuring they pass strict linting and follow accessibility (a11y) best practices.

## Core Workflows

### 1. Refactor Dependency Injection
Replace constructor-based injection with the `inject()` function to improve type safety and readability.
- Refer to [references/di-patterns.md](references/di-patterns.md) for examples.

### 2. Standardize Bootstrap JavaScript Integration
Ensure interactions with Bootstrap JS (e.g., Modals, Tooltips) are type-safe and SSR-compatible.
- Use `isPlatformBrowser` for window access.
- Avoid `any` by using standard Bootstrap interfaces.
- Refer to [references/bootstrap-types.md](references/bootstrap-types.md) for boilerplate.

### 3. Improve Template Accessibility (a11y)
Pass `@angular-eslint/template` rules by ensuring proper label associations and keyboard accessibility.
- Map every `<label>` to its control using `id` and `for`.
- Add `role="button"`, `tabindex`, and keyboard events to non-interactive clickable elements.
- Refer to [references/a11y-patterns.md](references/a11y-patterns.md) for patterns.

### 4. Cleanup and Type Refinement
Resolve common TypeScript linting errors:
- **Unused Code:** Remove unused imports and variables (`@typescript-eslint/no-unused-vars`).
- **Avoid `any`:** Replace `any` with specific models or `unknown`.
- **Empty Methods:** Delete empty lifecycle methods or placeholder functions.
- **Native Conflicts:** Rename `@Output` emitters that conflict with native DOM events (e.g. `toggle`, `select`).

### 5. Standalone Components Styling
When using standalone components, prefer element selectors over attribute selectors.
- Ensure proper `:host` styling (e.g., `display: table-row` for components used inside tables).

## Usage Examples

- **Refactor Task:** "Can you update this component to use the `inject()` function?"
- **Accessibility Task:** "Fix the label association errors in these templates."
- **Lint Task:** "Solve the 'Unexpected any' and 'Unused variable' errors in the services."
- **Bootstrap Task:** "Implement a type-safe modal open/close for this component."
