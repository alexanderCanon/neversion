# Arquitectura e Infraestructura — Neversion

Neversion corre sobre una arquitectura serverless-first, pensada para ser segura, escalable y barata de mantener.

## 1. Edge Layer y Seguridad Perimetral

Todo el tráfico entra por Cloudflare, que actúa como primera línea de defensa: CDN, WAF y rate limiting antes de que cualquier request llegue al backend.

* **API Gateway (Cloudflare Worker):** Valida cada JWT en el Edge y transforma las respuestas en view models limpios y optimizados para el frontend.
* **Automatización y Notificaciones (`telegram-reminder`):** En la misma capa Edge, Workers especializados como `telegram-reminder` ejecutan tareas programadas (CRON) para rastrear vencimientos y despachar recordatorios proactivos de renovación a través de Telegram sin la sobrecarga de mantener servidores dedicados 24/7.

## 2. Core Backend y Cómputo

Detrás del gateway, el backend corre en Spring Boot sobre instancias EC2 Graviton (ARM64), elegidas por costo, no por costumbre.

* **Infraestructura como Código (IaC):** La infraestructura completa está definida con Terraform.
* **CI/CD y Seguridad:** Cada despliegue pasa por un pipeline de CI/CD en GitHub Actions con ejecución de tests, linting, escaneo de vulnerabilidades de seguridad con Trivy y publicación hacia AWS vía OpenID Connect (OIDC) — sin una sola credencial estática almacenada.

## 3. Persistencia y Gestión de Datos

Los datos viven en PostgreSQL sobre Supabase, con Flyway gestionando cada cambio de esquema de forma versionada, controlada y reproducible.

## 4. Evolución y Filosofía de Diseño

La arquitectura evolucionó: empezó corriendo sobre un PaaS self-hosted por comodidad, hasta que evaluamos el costo real de mantener recursos ociosos. Hoy corre sobre infraestructura mínima y serverless — cada componente está ahí porque se necesita, no porque sea cómodo.
