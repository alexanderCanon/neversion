# Docker & CI/CD — Monorepo Neversion

## Contexto confirmado

| App | Stack | PM | Dockerfile | Serve mode |
|---|---|---|---|---|
| `apps/api` | Spring Boot 3 / Java 17 | Maven Wrapper | Existe, se mantiene | JVM |
| `apps/panel` | Angular 17 (sin SSR) | pnpm | Nuevo | Static + Nginx |
| `apps/store` | Angular 16 + nguniversal | npm | Nuevo | SSR + Node.js |

- **Dokploy:** Opcion B — conectado al repo de GitHub directamente, detecta merges a `main` y hace el deploy. El CI solo valida.
- **`packages/`:** Vacio — no se necesita pnpm workspace por ahora.

---

## Archivos a crear

### 1. `apps/api/Dockerfile`
**Sin cambios.** Ya es multistage y de alta calidad. Se conserva.
Los `compose.dev.yaml` y `compose.prod.yaml` dentro de `apps/api/` tambien se conservan.

---

### 2. `apps/panel/Dockerfile` [NEW]

Panel es admin interno, sin SSR. Build con pnpm, serve con Nginx.

- **Stage `build`:** `node:20-alpine` → instala pnpm → `pnpm install --frozen-lockfile` → `pnpm build` → output en `dist/panel/browser/`
- **Stage `run`:** `nginx:alpine` → copia static files → expone puerto `80`

Archivos adicionales:
- `apps/panel/nginx.conf` — config Nginx con `try_files` para SPA routing
- `apps/panel/.dockerignore` — excluye `node_modules/`, `.angular/`, `dist/`

---

### 3. `apps/store/Dockerfile` [NEW]

Store es sitio publico, SSR necesario para SEO.

- **Stage `build`:** `node:18-alpine` → `npm ci` → `npm run build:ssr` → output en `dist/neversion-site/{browser,server}/`
- **Stage `run`:** `node:18-alpine` slim → usuario no-root → `node dist/neversion-site/server/main.js` → expone `4000`

Archivos adicionales:
- `apps/store/.dockerignore` — excluye `node_modules/`, `dist/`

---

### 4. GitHub Actions — CI de validacion [NEW]

Dokploy hace el deploy. El CI solo **bloquea merges rotos** a `main`.
Corren en `pull_request` hacia `main` con path filters — cada workflow solo se activa si cambia su app.

**`.github/workflows/ci-api.yml`**
- Trigger: `pull_request` → `main`, paths: `apps/api/**`
- Job: Java 17 (temurin) → `./mvnw verify -B`

**`.github/workflows/ci-panel.yml`**
- Trigger: `pull_request` → `main`, paths: `apps/panel/**`
- Job: Node 20 → pnpm → `pnpm install --frozen-lockfile && pnpm build`

**`.github/workflows/ci-store.yml`**
- Trigger: `pull_request` → `main`, paths: `apps/store/**`
- Job: Node 18 → `npm ci && npm run build:ssr`

---

### 5. `compose.local.yml` [NEW] — raiz del monorepo

Levanta todo el stack local con un comando desde la raiz.

Servicios: `db` (postgres:17), `api`, `panel`, `store`.
Puertos locales: API `8080`, Panel `4200→80`, Store `4000`.

```
# Todo el stack:   docker compose -f compose.local.yml up --build
# Solo API + DB:   docker compose -f compose.local.yml up --build api db
# Solo panel:      docker compose -f compose.local.yml up --build panel
# Solo store:      docker compose -f compose.local.yml up --build store
```

---

### 6. `compose.prod.yml` [NEW] — raiz del monorepo (referencia)

Archivo de referencia/documentacion. **El deploy real es Dokploy individual.**

Documenta como configurar cada app en Dokploy:
1. Ir al panel de Dokploy → Nueva Application
2. Source → GitHub → seleccionar repo
3. Configurar el Dockerfile path por app:
   - API: `apps/api/Dockerfile`
   - Panel: `apps/panel/Dockerfile`
   - Store: `apps/store/Dockerfile`
4. Configurar env vars en Dokploy por app
5. Dokploy detecta automaticamente los merges a `main` y redespliega

Servicios con variables `${VAR}` para todos los secretos.

---

## Sobre pnpm workspace

**No es necesario ahora.** `packages/` esta vacio, las apps son completamente independientes.

Tendra sentido cuando `packages/` tenga codigo compartido (tipos, DTOs, componentes UI).
En ese momento: root `package.json` con workspaces + `pnpm-workspace.yaml` + migrar `store` de npm a pnpm.

---

## Verification

| Paso | Comando | Esperado |
|---|---|---|
| Build API | `docker build -t api-test apps/api` | imagen OK |
| Build panel | `docker build -t panel-test apps/panel` | imagen OK |
| Build store | `docker build -t store-test apps/store` | imagen OK |
| Stack completo | `docker compose -f compose.local.yml up --build` | 4 servicios UP |
| CI path filter | PR con cambio solo en `apps/panel/**` | Solo `ci-panel.yml` se activa |
