# Architecture Design - Entity Relationship Diagram

This diagram represents the unified data model for the Neversion system (Sprint 1.5). It merges the flexible layout from the new automation database while retaining the essential components for storefront features (such as Reservations and Orders).

```mermaid
erDiagram
    SERVICES ||--o{ ACCOUNTS : organizes
    ACCOUNTS ||--o{ PROFILES : contains
    CLIENTS ||--o{ SUBSCRIPTIONS : has
    PROFILES ||--o{ SUBSCRIPTIONS : assigned_to

    %% Legacy Storefront Entities (Bridged to new model)
    CLIENTS ||--o{ RESERVATIONS : creates
    RESERVATIONS ||--o{ ORDERS : generates

    SERVICES {
        int id PK
        string name
        int max_profiles
    }

    ACCOUNTS {
        int id PK
        int service_id FK
        string email
        string password
        date renewal_date
        string plan
        string sale_mode
        text notes
    }

    PROFILES {
        int id PK
        int account_id FK
        string name
        string pin
        boolean is_owner
    }

    CLIENTS {
        int id PK
        string name
        string phone
        string email
        text notes
    }

    SUBSCRIPTIONS {
        int id PK
        int client_id FK
        int profile_id FK
        date start_date
        date payment_due_date
        int months_paid
        string status
    }

    NOTIFICATION_LOG {
        int id PK
        string entity_type
        int entity_id
        string stage
        timestamp sent_at
    }

    RESERVATIONS {
        UUID id PK
        int client_id FK
        numeric discount
        numeric total
        string receipt_url
        timestamp expiration_date
    }

    ORDERS {
        UUID id PK
        UUID reservation_id FK
        string status
        text notes
    }
```

## Entity Descriptions

- **SERVICES**: Specific platforms offered (e.g., Netflix, Disney+, Spotify).
- **ACCOUNTS**: The physical master credentials purchased from a third-party wholesale provider to operate a service.
- **PROFILES**: Direct physical sub-divisions of an Account (formerly known as "Slots"). 
- **CLIENTS**: The consumers paying for profiles. Replaces the legacy "Users Guests".
- **SUBSCRIPTIONS**: The core relationship between a Client and a specific Profile, controlling their access window and payment dates.
- **NOTIFICATION_LOG**: Used by async jobs (n8n) to track notification stages (e.g., generated emails for renewals).
- **RESERVATIONS & ORDERS**: Used primarily for storefront workflow (Phase 3). Contains payment receipts and temporary checkout state.
