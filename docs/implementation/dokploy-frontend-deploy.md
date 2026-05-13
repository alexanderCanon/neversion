# Frontend Deployment

## Panel — Cloudflare Pages

Recommended Cloudflare Pages configuration:

```text
Source: GitHub repo
Branch: main
Root directory: /
Build command: pnpm --filter panel build:cloudflare
Build output directory: apps/panel/dist/panel/browser
```

Environment variables:

```text
PANEL_API_URL=https://api.example.com
PANEL_SUPABASE_URL=https://project.supabase.co
PANEL_SUPABASE_KEY=<supabase-anon-key>
GRAFANA_URL=https://grafana.example.com
```

Notes:

- `build:cloudflare` generates `src/assets/runtime-config.js` before `ng build`.
- `_redirects` provides the SPA fallback for Angular routes.
- `_headers` provides security headers and disables caching for
  `/assets/runtime-config.js`.

## Panel — Dokploy legacy fallback

Previous Dokploy configuration:

```text
Source: GitHub repo
Branch: main
Watch Path: apps/panel/**,packages/**
Compose file path: apps/panel/compose.prod.yml
Build path: .
Internal port: 80
```

Environment variables:

```text
PANEL_API_URL=https://api.example.com
PANEL_SUPABASE_URL=https://project.supabase.co
PANEL_SUPABASE_KEY=<supabase-anon-key>
GRAFANA_URL=https://grafana.example.com
```

## Store

Recommended Dokploy configuration:

```text
Source: GitHub repo
Branch: main
Watch Path: apps/store/**,packages/**
Compose file path: apps/store/compose.prod.yml
Build path: .
Internal port: 4000
```

Environment variables:

```text
STORE_API_URL=https://api.example.com
STORE_SUPABASE_URL=https://project.supabase.co
STORE_SUPABASE_KEY=<supabase-anon-key>
STORE_VENDOR_UUID=<vendor-public-uuid>
```

## Notes

- Dokploy compose files build from the repository root because both apps depend on
  shared workspace packages.
- Dokploy runtime browser config is generated at container startup.
- `PANEL_API_URL` and `STORE_API_URL` should be browser-accessible public API
  URLs, not internal Docker hostnames.
