# Estrategia de Refactorización de Arquitectura Backend (Spring Boot, Supabase, Cloudflare)

## 1. Visión General

Con el objetivo de maximizar el rendimiento, minimizar el consumo de recursos (CPU/RAM/hilos) en el backend en **Spring Boot (`apps/api`)** y mantenerlo enfocado en su fortaleza transaccional **ACID**, se definió una re-arquitectura estratégica delegando tareas a la infraestructura Serverless y Edge de **Supabase** y **Cloudflare**.

---

## 2. Resumen de Prioridades

| Prioridad | Componente / Dominio | Estrategia Seleccionada | Estado |
| :--- | :--- | :--- | :--- |
| **Prioridad 1 (Alta)** | **Notificaciones de Renovación (7d, 3d, 1d, 0d)** | **Supabase Edge Functions (Deno/TS) + Resend API + Cron Trigger** | **Completado & Probado** |
| **Prioridad 2 (Media)** | **API Gateway & Load Balancer** | **Cloudflare Workers (Edge JWT HS256 + Round Robin) + Spring Header Filter** | **Completado & Compilado** |
| **Prioridad 3 (Baja)** | **Dashboard Analítico** | **PostgreSQL Views / RPC + Supabase PostgREST + RLS (0 servidores)** | **Pendiente (Siguiente Sesión)** |

---

## 3. Detalle de Implementación: Prioridad 1 (Notificaciones Edge)

### Desafío Original
Spring Boot mantenía un agendador Java `@Scheduled` (`RenewalReminderScheduler.java`) que llamaba mediante **gRPC** a un microservicio en Rust (`apps/notification-service`). Esto mantenía procesos en segundo plano e hilos abiertos en Java y un microservicio adicional corriendo.

### Solución Implementada
1. **Supabase Edge Function** ([supabase/functions/send-renewal-reminders/index.ts](file:///home/alexander/projects/neversion/supabase/functions/send-renewal-reminders/index.ts)):
   - Despierta automáticamente **1 sola vez al día a las 08:00 AM UTC** mediante la configuración en [supabase/config.toml](file:///home/alexander/projects/neversion/supabase/config.toml).
   - Consulta suscripciones activas y cuentas maestras por vencer.
   - Resuelve el correo del usuario vendedor directamente desde Supabase Auth (`auth.users.email`) mediante `getUserById`.
   - Verifica deduplicación en `notification_log` (`entity_type`, `entity_id`, `stage`).
   - Envía el correo mediante `https://api.resend.com/emails` pasando el `template.id` de Resend y las variables Handlebars en `camelCase` (`clientName`, `serviceName`, `paymentDueDate`, `daysRemaining`, `accountEmail`, `storeName`, `renewalDate`) convertidas a `String`.
2. **Spring Boot Cleanup**:
   - `RenewalReminderScheduler.java` deshabilitado por defecto (`neversion.cron.renewal-reminders.enabled=false`).
   - `GrpcNotificationLogAdapter.java` deshabilitado por defecto (`neversion.grpc.notification-service.enabled=false`).
   - `JpaNotificationLogAdapter.java` marcado con `@Primary`.
   - Microservicio `apps/notification-service` queda marcado para retiro.

---

## 4. Detalle de Implementación: Prioridad 2 (Cloudflare Workers API Gateway)

### Desafío Original
Spring Boot ejecutaba `spring-boot-starter-oauth2-resource-server` con `NimbusJwtDecoder` en Java, decodificando y verificando tokens HMAC HS256 en cada petición HTTP entrante.

### Solución Implementada
1. **Cloudflare Worker API Gateway** ([apps/api-gateway/src/index.ts](file:///home/alexander/projects/neversion/apps/api-gateway/src/index.ts)):
   - Valida la firma y expiración del JWT de Supabase Auth en el **Edge de Cloudflare (<1ms)** usando Web Crypto API.
   - Rechaza peticiones inválidas con `HTTP 401 Unauthorized` de inmediato sin tocar los servidores backend.
   - Extrae `sub` (userId) y `role` e inyecta los encabezados `X-User-Id`, `X-User-Role` y `X-Gateway-Secret`.
   - Balancea el tráfico entre orígenes (`BACKEND_ORIGINS`) usando el algoritmo **Round Robin**.
2. **Filtro HTTP en Spring Boot** ([GatewayHeaderAuthenticationFilter.java](file:///home/alexander/projects/neversion/apps/api/src/main/java/com/neversion/api/config/GatewayHeaderAuthenticationFilter.java)):
   - Lee `X-User-Id` y `X-User-Role` e inyecta un `JwtAuthenticationToken` sintético en el `SecurityContext` de Spring.
   - Mantiene 100% de compatibilidad con todas las anotaciones y reglas RBAC existentes en Spring Security (`hasRole('VENDOR')`, `hasRole('CLIENT')`) con **0 overhead de decodificación JWT en Java**.

---

## 5. Plan para la Próxima Sesión: Prioridad 3 (PostgREST Dashboard)

En la siguiente sesión (en la rama `refactor1` antes del PR a `main`):
1. Crear vistas y funciones RPC en PostgreSQL (`V40__create_dashboard_analytics_views.sql`).
2. Conectar el frontend del Panel Admin en Angular (`apps/panel`) directamente a PostgREST con seguridad RLS.
3. Eliminar el paquete `com.neversion.api.dashboard` de Spring Boot para liberar completamente el backend Java de tareas analíticas.
