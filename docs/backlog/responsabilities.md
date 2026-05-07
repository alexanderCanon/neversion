# Responsabilidades de Agentes

Este documento define el reparto de tareas y las reglas de colaboración entre los diferentes agentes especializados de IA que participan en el proyecto.

---

## Matriz de Responsabilidades

| Agente | Responsabilidad Principal | Rama Git |
| :--- | :--- | :--- |
| **Agent Foundation** | EPIC-00 completa — migraciones y refactorización base. | `feature/foundation` |
| **Agent Backend** | Lógica de negocio, endpoints, seguridad y tests (un módulo a la vez). | `feature/backend` |
| **Agent Panel** | Implementación del Panel del Vendedor y Super Admin (Angular). | `feature/panel` |
| **Agent Store** | Implementación de la Tienda y Panel del Cliente (Angular). | `feature/store` |
| **Agent Notifications** | Módulo de correos transaccionales y `notification_log`. | `feature/notifications` |

---

## Reglas de Trabajo (Workflow)

> [!IMPORTANT]
> **Prioridad de Foundation**: `agent-foundation` trabaja primero y en solitario. Nadie más toca las migraciones de base de datos durante esta fase.

> [!NOTE]
> **Aislamiento de Backend**: `agent-backend` recibe un módulo a la vez como contexto para evitar inconsistencias; no trabaja todo el backend de forma masiva.

> [!TIP]
> **Sincronización Frontend**: `agent-panel` y `agent-store` arrancan únicamente cuando los endpoints del módulo correspondiente en `feature/backend` han sido finalizados.

> [!WARNING]
> **Paralelismo**: `agent-notifications` puede trabajar en paralelo con el backend una vez que la **EPIC-00** haya sido integrada en `develop`.

---

## Integración
La integración de todas las funcionalidades debe realizarse siempre a través de la rama `develop` antes de ser promovida a `main`.