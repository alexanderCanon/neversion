# High-Level Architecture Overview

This diagram provides a high-level view of the decoupled, microservice-ready architecture used by the Neversion system.

```mermaid
graph TD
    subgraph Client_Layer [Client Layer]
        A[Web App - React/Next.js]
    end

    subgraph API_Gateway [Entry & Security]
        B[API Gateway / Load Balancer]
    end

    subgraph Services [Core Services]
        C[Service: Orders & Cart]
        D[Service: Auth & Users]
        E[Service: Catalog]
    end

    subgraph Storage [Persistence & Files]
        F[(Relational DB - PostgreSQL)]
        G[Object Storage - S3/Cloudinary]
    end

    subgraph Workers [Asynchronous Processes]
        H[Timer Worker - Redis/Cron]
        I[Notification Service - Email/WhatsApp]
    end

    A --> B
    B --> C
    B --> D
    B --> E
    C --> F
    C --> G
    C --> H
    H -- "Expires order after 1h" --> F
    I -- "Notifies Admin/Client" --> A
```

## Architectural Components

- **Client Layer**: Modern web frontend using React/Next.js, providing a dynamic shopping experience.
- **API Gateway**: Acts as the single point of entry, handling authentication, load balancing, and routing.
- **Core Services**: Decoupled modules managing specialized domain logic for catalogs, users, and orders.
- **Persistence & Files**: Secure storage for relational data in PostgreSQL and media assets in Object Storage (S3).
- **Asynchronous Processes**: Handles background tasks such as order expiration timers and real-time notifications via Email or messaging.
