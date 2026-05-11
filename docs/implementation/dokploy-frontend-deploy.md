# Dokploy Frontend Deployment

## Panel

Recommended Dokploy configuration:

```text
Source: GitHub repo
Branch: front/panel
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
Branch: front/panel
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

- The compose files build from the repository root because both apps depend on
  shared workspace packages.
- Runtime browser config is generated at container startup.
- `PANEL_API_URL` and `STORE_API_URL` should be browser-accessible public API
  URLs, not internal Docker hostnames.
