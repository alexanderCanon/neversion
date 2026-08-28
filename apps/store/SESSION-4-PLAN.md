# Sesión 4 — Plan Detallado + Siguientes Pasos

> Documento de trabajo generado el 27/08/2026 al cierre de las sesiones 1-3.

---

## Sesión 4: Calidad y Polish

### 4.1 — Placeholder "Comprobantes" en Customer Panel

**Qué hacer:**
- Abrir `src/pages/CustomerPanel.tsx`
- Verificar si existe un tab "Comprobantes" actualmente
- Si no existe, agregar un tab con el contenido:
  ```
  🛠️ Función en desarrollo
  Próximamente podrás consultar tus comprobantes de pago directamente desde aquí.
  ```
- Mantener el estilo consistente con los demás tabs del panel

**Archivos a modificar:** `src/pages/CustomerPanel.tsx`
**Tiempo estimado:** ~15 min

---

### 4.2 — Página `/offers` (Placeholder)

**Qué hacer:**
- Crear `src/pages/Offers.tsx` con un diseño de "Función futura":
  ```
  🎁 Ofertas y Promociones
  Estamos preparando ofertas exclusivas para ti. ¡Muy pronto!
  ```
- Registrar la ruta `/offers` en `src/router.tsx`
- Agregar link a la Navbar si aplica

**Archivos a crear:** `src/pages/Offers.tsx`
**Archivos a modificar:** `src/router.tsx`
**Tiempo estimado:** ~10 min

---

### 4.3 — Configurar Vitest

**Qué hacer:**
1. Agregar devDependencies: `vitest`, `@testing-library/react`, `@testing-library/jest-dom`, `jsdom`
2. Crear `vitest.config.ts` o agregar config en `vite.config.ts`
3. Agregar script `"test": "vitest run"` y `"test:watch": "vitest"` en `package.json`
4. Crear un test de smoke básico (`src/App.test.tsx` o similar) que valide que App renderiza sin errores
5. Crear `src/setupTests.ts` con `@testing-library/jest-dom` imports

**Archivos a crear:** `vitest.config.ts` (o config en vite), `src/App.test.tsx`, `src/setupTests.ts`
**Archivos a modificar:** `package.json`
**Tiempo estimado:** ~20 min

---

## Hallazgos Adicionales (Post Sesión 4)

### ⚠️ Navbar usa anchor links en lugar de rutas dedicadas

Actualmente en `src/components/layout/Navbar.tsx` (línea 23-30):
```ts
const navLinks = [
  { label: 'Inicio', href: '/' },
  { label: 'Plataformas', href: '/#plataformas' },   // ← scroll anchor
  { label: 'Combos', href: '/#combos' },               // ← scroll anchor
  { label: 'Juegos', href: '/#juegos' },                // ← scroll anchor
  { label: 'Recargas', href: '/#recargas' },
  { label: 'Mayoristas', href: '/wholesalers' },
]
```

**Problema:** Ahora que existen las páginas dedicadas `/platforms`, `/games`, `/combo`, la Navbar
debería linkear a ellas directamente en vez de hacer scroll en el Home.

**Acción sugerida:** Actualizar a:
```ts
const navLinks = [
  { label: 'Inicio', href: '/' },
  { label: 'Plataformas', href: '/platforms' },
  { label: 'Combos', href: '/combo' },
  { label: 'Juegos', href: '/games' },
  { label: 'Mayoristas', href: '/wholesalers' },
]
```

### ⚠️ Archivos sobrantes del proyecto standalone

Los siguientes archivos vienen del proyecto standalone en `~/Descargas` y deberían
limpiarse o adaptarse ahora que `new-store` vive dentro del monorepo:

| Archivo | Acción |
|---|---|
| `AGENTS.md` | Eliminar (el monorepo ya tiene su propio AGENTS.md raíz) |
| `CLAUDE.md` | Eliminar (referencia a AGENTS.md que ya no aplica) |
| `pnpm-workspace.yaml` | Eliminar (el workspace se gestiona desde la raíz del monorepo) |
| `.npmrc` | Revisar si es necesario (tiene auth token para GitHub Packages de @alexandercanon) |
| `.mise.toml` | Eliminar o mover a raíz (define Node 24 + pnpm 11) |
| `.env` | Renombrar a `.env.example` y vaciar los valores sensibles (Supabase keys) |

### ⚠️ Verificación de build

Antes de considerar el trabajo completo, se debe:
1. `pnpm install` desde la raíz del monorepo (resuelve `@neversion/models` workspace)
2. `cd apps/new-store && pnpm build` (verifica que Vite compila sin errores)
3. Verificar visualmente que las nuevas páginas renderizan correctamente

### ⚠️ Deployment workflow

El workflow actual de GitHub Actions para `apps/store` apunta al proyecto Angular.
Cuando `new-store` reemplace a `store`, se necesitará:
- Actualizar el workflow para usar `vite build` en vez de `ng build`
- Ajustar el directorio de output (`dist/` en vez de `dist/store/browser`)
- Verificar que las env vars de CF Pages mapean a las `VITE_*` correctas

---

## Resumen Final del Trabajo de Hoy

### Objetivo
Determinar qué le faltaba a `~/Descargas/new-store` para reemplazar `apps/store` y cerrar las brechas.

### Lo que se hizo

**Análisis (1 artefacto)**
- Comparación exhaustiva entre Angular store y React new-store
- Identificación de 14 gaps priorizados
- Plan aprobado por Alex con feedback item por item

**Sesión 1 — Fundamentos (commit `e7dd456`)**
- ✅ `roleGuard` en `ProtectedRoute` con `allowedRoles` prop
- ✅ `resolveServiceImageUrl()` en `lib/image.ts`
- ✅ Integración `@neversion/models` (re-export `User`, `UserRole`, `AuthResult`)

**Sesión 2 — Páginas de catálogo (commit `5fcc963`)**
- ✅ `/platforms` — Catálogo con búsqueda + filtro por categoría
- ✅ `/platforms/:platformId` — Detalle con imagen, features, sidebar de precios
- ✅ `/games` — Catálogo dedicado de juegos
- ✅ `/combo` — Explicación de combos con tiers dinámicos del vendor

**Sesión 3 — UX y branding (commits `9d2c25e`, `0b9d804`)**
- ✅ `FloatingWhatsApp` FAB en todas las páginas
- ✅ Sistema de toast (`ToastContext` + `ToastContainer` + `useToast`)
- ✅ 10 assets SVG/PNG migrados a `public/assets/`
- ✅ ESLint config (typescript-eslint + react-hooks)
- ✅ `.editorconfig` consistente con el monorepo

### Métricas
- **5 commits** en rama `feat/new-store-parity`
- **12 archivos nuevos** + **7 archivos modificados** + **10 assets migrados**
- **~1,300 líneas de código** nuevo (páginas + componentes + contextos)

### Lo que queda (Sesión 4 — estimado ~45 min)
1. Placeholder Comprobantes en CustomerPanel
2. Placeholder `/offers`
3. Configurar Vitest
4. Actualizar links de Navbar a rutas dedicadas
5. Limpiar archivos standalone sobrantes
6. Verificar build

### Lo que NO se tocó (fuera de scope)
- `apps/store` (el Angular original permanece intacto)
- `apps/api` / backend
- `docs/` (read-only)
- Deployment workflow (responsabilidad de Alex)
