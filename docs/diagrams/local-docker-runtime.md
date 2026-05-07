# Runtime Local Con Docker Compose

Este diagrama describe como interactuan los contenedores cuando se levanta el stack local desde `infra/compose.local.yml`.

```mermaid
flowchart LR
    Developer["Alex / Browser"]

    subgraph DockerHost["Docker local"]
        subgraph Compose["infra/compose.local.yml"]
            DB[("neversion-db<br/>PostgreSQL 17<br/>localhost:5432")]

            API["neversion-api<br/>Spring Boot<br/>:8080"]

            Panel["neversion-panel<br/>Angular static + Nginx<br/>localhost:4200"]

            Store["neversion-store<br/>Angular SSR + Node/Express<br/>localhost:4000"]
        end
    end

    subgraph External["Servicios externos"]
        SupabaseAuth["Supabase Auth<br/>OAuth / JWT issuer"]
        Resend["Resend<br/>Email API"]
    end

    Developer -->|"http://localhost:4200"| Panel
    Developer -->|"http://localhost:4000"| Store
    Developer -->|"http://localhost:8080/api/v1"| API

    Panel -->|"Nginx proxy /api/*<br/>http://api:8080"| API
    Store -->|"Browser HTTP<br/>STORE_API_URL"| API

    API -->|"JDBC<br/>SPRING_DATASOURCE_*"| DB

    Panel -.->|"Supabase client<br/>PANEL_SUPABASE_URL / KEY"| SupabaseAuth
    Store -.->|"Supabase client<br/>STORE_SUPABASE_URL / KEY"| SupabaseAuth

    SupabaseAuth -.->|"JWT access token"| Panel
    SupabaseAuth -.->|"JWT access token"| Store

    Panel -->|"Authorization: Bearer JWT"| API
    Store -->|"Authorization: Bearer JWT"| API
    API -.->|"Verifica firma<br/>SUPABASE_JWT_SECRET"| SupabaseAuth

    API -.->|"Emails si aplica<br/>RESEND_API_KEY"| Resend
```

## Puntos Clave

- `db`, `api`, `panel` y `store` corren dentro de la misma red Docker Compose.
- La API usa `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/neversiondb` para conectarse al PostgreSQL local.
- El panel es estatico y Nginx sirve los assets; tambien proxya `/api/*` hacia `http://api:8080`.
- El store es SSR y corre con el servidor Node/Express generado por Angular Universal.
- Los frontends usan `runtime-config.js`, generado al arrancar el contenedor, para leer URLs y keys desde variables de entorno.
- Supabase Auth se usa para autenticacion; la API valida tokens con `SUPABASE_JWT_SECRET`.
- En produccion la topologia cambia: API, panel y store se despliegan por separado en Dokploy, y la base de datos real se configura con `SPRING_DATASOURCE_*`.

## Comando Local

```bash
cp infra/.env.example infra/.env
docker compose --env-file infra/.env -f infra/compose.local.yml up --build
```
