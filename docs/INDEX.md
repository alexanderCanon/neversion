# Neversion — Documentación del Proyecto

Bienvenido a la documentación técnica de **Neversion**, la plataforma SaaS para la gestión y reventa de servicios digitales.

## Mapa de Navegación

### 1. [Discovery](./discovery/discovery.md)
Resumen ejecutivo, visión del producto, alcance del MVP y objetivos estratégicos.

### 2. Dominio
*   [**Glosario de Lenguaje Ubicuo**](./domain/ubiquitous-language.md): Definiciones de términos clave.
*   [**Modelo de Dominio**](./domain/domain-model.md): Estructura de entidades y responsabilidades.
*   [**Mapa de Actores y Roles**](./domain/actors.md): Definición de Super Admin, Vendedor y Cliente.
*   [**Reglas de Negocio**](./domain/business-rules.md): Lógica fundamental y restricciones del sistema.
*   [**Catálogo de Casos de Uso**](./domain/use-cases.md): Flujos principales de interacción.

### 3. Arquitectura
*   [**Decisiones Arquitectónicas (ADRs)**](./architecture/decisions.md): Registro de decisiones técnicas clave.
*   [**Diagrama Entidad-Relación (ER)**](./architecture/er-diagram.md): Estructura de la base de datos.
*   [**Análisis de Brechas (Gap Analysis)**](./architecture/gap-analysis.md): Comparativa Discovery vs Implementación.
*   [**Requerimientos No Funcionales (NFR)**](./architecture/nfr.md): Seguridad, rendimiento y escalabilidad.
*   [**Visión de Despliegue**](./architecture/deployment.md): Topología del sistema y ambientes.

### 4. Diagramas y Flujos
*   [**Arquitectura de Alto Nivel**](./diagrams/architecture.md): Topología de frontends, workers edge y servicios.
*   [**Flujo Principal (End-to-End)**](./diagrams/main-flow.md): Secuencia completa desde catálogo hasta entrega.
*   [**Ciclo de Vida de Suscripciones**](./diagrams/subscription-lifecycle.md): Estados y transiciones de suscripciones.
*   [**Sistema de Notificaciones**](./diagrams/notification-system.md): Arquitectura de despacho transaccional y recordatorios.
*   [**API Gateway Edge**](./diagrams/api-gateway.md): Validación de tokens y balanceo perimetral.

### 5. Backlog
*   [**Épicas**](./backlog/epics.md): Listado de grandes bloques de trabajo.
*   [**Historias de Usuario**](./backlog/stories/): Detalle de requerimientos funcionales por épica.
*   [**Responsabilidades de Agentes**](./backlog/responsabilities.md): Reparto de tareas y reglas de trabajo.

### 6. Seguimiento e Implementación (Histórico)
*   [**Roadmap de Documentación**](./documentation-roadmap.md): Brechas y ambigüedades resueltas.
*   [**Bitácoras de Implementación**](./implementation/README.md): Registro histórico de construcción de backend, panel y storefront.

### 7. Protocolos de Agentes AI
*   [**AGENTS.md**](../AGENTS.md): Reglas de colaboración, branch protocol y estándares para agentes.
*   [**CLAUDE.md**](../CLAUDE.md): Directrices técnicas de desarrollo del monorepo.

---
*Última actualización: Septiembre 2026*

