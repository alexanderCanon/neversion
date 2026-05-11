# Monitoring Deployment Notes

## Services

Production monitoring is deployed as separate Dokploy apps:

- `neversion-api`: Spring Boot API exposing `/actuator/prometheus`.
- `neversion-prometheus`: private Prometheus service scraping the API.
- `neversion-grafana`: Grafana service exposed with HTTPS and login.

## Network

Prometheus and Grafana must share a private Docker network with the API or use
the internal service names provided by Dokploy. The target in
`prometheus.yml` must resolve from the Prometheus container:

```yaml
targets: ["neversion-api-app:8080"]
```

Adjust the service name if Dokploy assigns a different internal DNS name.

## Dokploy Prometheus App

Recommended Dokploy configuration:

```text
Source: GitHub repo
Branch: main
Watch Path: apps/api/monitoring/**
Compose file path: apps/api/monitoring/prometheus.compose.yml
Build path: apps/api/monitoring
```

Environment variables:

```text
NEVERSION_MONITORING_SCRAPE_TOKEN=<strong-random-token>
NEVERSION_API_TARGET=neversion-api:8080
```

Adjust `NEVERSION_API_TARGET` only if Dokploy assigns a different internal DNS
name to the API service.

## Dokploy Grafana App

Recommended Dokploy configuration:

```text
Source: GitHub repo
Branch: main
Watch Path: apps/api/monitoring/**
Compose file path: apps/api/monitoring/grafana.compose.yml
Build path: apps/api/monitoring
```

Environment variables:

```text
GF_SECURITY_ADMIN_USER=<admin-user>
GF_SECURITY_ADMIN_PASSWORD=<strong-password>
GRAFANA_URL=https://grafana.example.com
```

Expose Grafana through Dokploy/Traefik with HTTPS. Do not enable anonymous
access. Grafana reads Prometheus through the internal Docker network using the
`http://prometheus:9090` datasource from `grafana/datasources.yml`.

## Secrets

The API and Prometheus must share the same scrape token:

```text
NEVERSION_MONITORING_SCRAPE_TOKEN=<strong-random-token>
```

The token is sent as:

```text
Authorization: Bearer <token>
```

## Exposure Rules

- `/actuator/health` remains public for health checks.
- `/actuator/prometheus` accepts the Prometheus service token or a
  `SUPER_ADMIN` JWT.
- Other `/actuator/**` endpoints remain restricted to `SUPER_ADMIN`.
- Prometheus should not be exposed publicly.
- Grafana may be exposed publicly only with HTTPS and authentication enabled.
