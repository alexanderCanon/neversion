# Dependency Injection Patterns (Angular 17+)

## Core Pattern: Using `inject()`

Angular 17 prefers the `inject()` function over constructor-based parameter injection for better type inference and clarity.

### Before: Constructor Injection
```typescript
constructor(
  private readonly fb: FormBuilder,
  private readonly authService: AuthService
) {}
```

### After: `inject()` Function
```typescript
private readonly fb = inject(FormBuilder);
private readonly authService = inject(AuthService);

constructor() {
  // Constructor can remain for simple logic (e.g. initialization)
}
```

## Special Cases: Injection Tokens

For tokens like `PLATFORM_ID`, use `inject(PLATFORM_ID)`.

### Before
```typescript
constructor(@Inject(PLATFORM_ID) private platformId: object) {}
```

### After
```typescript
private readonly platformId = inject(PLATFORM_ID);
```
