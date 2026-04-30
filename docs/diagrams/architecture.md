# Arquitectura de Alto Nivel

Este diagrama describe la interacción entre los componentes del monorepo y los servicios externos.

```mermaid
graph TD
    subgraph Client_Layer [Capa de Cliente]
        Store[Store App - Angular 16]
        Panel[Panel App - Angular 17]
    end

    subgraph Auth_Service [Identidad]
        Supabase[Supabase Auth]
    end

    subgraph Backend_Layer [Capa de Aplicación]
        API[Spring Boot API]
        Client_Lib[API Client - Generated TS]
        Worker[Notification Worker]
    end

    subgraph Persistence_Layer [Capa de Datos]
        DB[(PostgreSQL)]
    end

    subgraph Notifications [Notificaciones]
        Resend[Resend - Email API]
        n8n[n8n / WhatsApp]
    end

    Store -->|Consume| Client_Lib
    Panel -->|Consume| Client_Lib
    Client_Lib -->|REST / JWT| API
    
    API -->|JPA / Flyway| DB
    Worker -->|Procesa pending| DB
    
    Store -.->|Auth| Supabase
    Panel -.->|Auth| Supabase
    
    Worker -.->|Send| Resend
    API -.->|Events| n8n
    n8n -.->|WhatsApp| Store
```

## Componentes
1.  **Store App:** Interfaz para el cliente final (Catálogo, Carrito, Mis Accesos).
2.  **Panel App:** Interfaz administrativa para Vendedores (Inventario, Órdenes, Clientes).
3.  **Supabase Auth:** Gestiona la autenticación externa y provee el `externalId` (UUID).
4.  **Spring Boot API:** Núcleo del sistema (Reglas de negocio, Hexagonal Architecture).
5.  **Notification Worker:** Proceso interno que consume `notification_log` (PENDING) y envía correos vía Resend (EPIC-08).
6.  **Resend:** Servicio externo de envío de correos transaccionales.
7.  **API Client:** Paquete compartido que garantiza contratos tipados entre el backend y los frontends.
8.  **n8n:** Orquestador de notificaciones externas (WhatsApp).

