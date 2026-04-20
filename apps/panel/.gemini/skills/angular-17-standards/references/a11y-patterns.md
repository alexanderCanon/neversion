# Template Accessibility (a11y) Patterns

Angular 17 projects must follow these accessibility patterns to pass linting and ensure usability.

## Pattern 1: Label Association

Associate every `<label>` with its corresponding form control using `id` and `for`.

### Before
```html
<label class="form-label">Search</label>
<input type="text" class="form-control" />
```

### After
```html
<label for="searchId" class="form-label">Search</label>
<input id="searchId" type="text" class="form-control" />
```

## Pattern 2: Interactive Non-Input Elements

Add `tabindex`, `role`, and keyboard event handlers when using `(click)` on non-interactive elements (e.g. `<div>`).

### Before
```html
<div class="row" (click)="toggle()"> ... </div>
```

### After
```html
<div class="row" 
     role="button" 
     tabindex="0"
     (click)="toggle()"
     (keydown.enter)="toggle()"
     (keydown.space)="toggle(); $event.preventDefault()"
     aria-expanded="false"> 
  ... 
</div>
```

## Pattern 3: Invalid Labels

Do not use `<label>` for text that doesn't belong to a form control. Use `<span>` or `<div>` instead.

### Before
```html
<th class="fw-semibold"><label>#</label> PIN</th>
```

### After
```html
<th class="fw-semibold"><span>#</span> PIN</th>
```
