# Monitoring Deployment Notes

## Services

Production monitoring is deployed as separate Dokploy apps:

- `neversion-api`: Spring Boot API exposing `/actuator/prometheus`.
- `neversion-prometheus`: private Prometheus service scraping the API.
- `neversion-grafana`: Grafana service exposed with HTTPS and login.
- `neversion-alloy`: lightweight Grafana Alloy service scraping the API and
  sending metrics to Grafana Cloud.

For a 4 GB VPS, prefer Grafana Cloud + Alloy over local Prometheus + Grafana.
Do not run both paths at the same time unless you intentionally want duplicate
scrapes and higher resource usage.

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

## Dokploy Alloy App — Grafana Cloud

Recommended Dokploy configuration:

```text
Source: GitHub repo
Branch: main
Watch Path: apps/api/monitoring/**
Compose file path: apps/api/monitoring/alloy.compose.yml
Build path: apps/api/monitoring
```

Environment variables:

```text
NEVERSION_MONITORING_SCRAPE_TOKEN=<same-token-as-api>
NEVERSION_API_TARGET=neversion-api:8080
GRAFANA_CLOUD_REMOTE_WRITE_URL=https://prometheus-xxx.grafana.net/api/prom/push
GRAFANA_CLOUD_PROMETHEUS_USERNAME=<metrics-instance-id>
GRAFANA_CLOUD_LOKI_URL=https://logs-prod-xxx.grafana.net/loki/api/v1/push
GRAFANA_CLOUD_LOKI_USERNAME=<logs-instance-id>
GRAFANA_CLOUD_API_KEY=<cloud-access-policy-token-with-metrics-and-logs-write>
```

Use the same `NEVERSION_MONITORING_SCRAPE_TOKEN` configured in the API app.
Alloy connects to the shared `neversion-network` Docker network and scrapes:

```text
http://NEVERSION_API_TARGET/actuator/prometheus
```

Validation in Grafana Cloud Explore:

```promql
up{job="neversion-api"}
jvm_memory_used_bytes
http_server_requests_seconds_count
```

If Alloy is active, keep local Prometheus and Grafana stopped to save RAM.

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
