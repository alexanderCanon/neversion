# E-R Diagram

## Notas de Esquema

### Estructura de `discount_cfg` (JSONB) en VENDORS
```json
{
  "min_items": 2,
  "tiers": [
    { "from": 2, "to": 3, "discount_pct": 5 },
    { "from": 4, "to": null, "discount_pct": 10 }
  ]
}
```
El descuento se calcula sobre la cantidad total de ítems en el carrito (incluyendo servicios repetidos). Ver BR-13.

---

```mermaid
erDiagram
    USERS {
        bigint id PK
        uuid uuid
        string external_id
        string role
        timestamp created_at
    }

    VENDORS {
        bigint id PK
        uuid uuid
        bigint user_id FK
        bigint vendor_id FK
        string store_name
        string logo_url
        jsonb bank_details
        jsonb discount_cfg
        timestamp created_at
    }

    CLIENTS {
        bigint id PK
        uuid uuid
        bigint user_id FK
        bigint vendor_id FK
        string name
        string phone
        string email
        text notes
        timestamp created_at
    }

    SERVICES {
        bigint id PK
        uuid uuid
        bigint vendor_id FK
        string name
        string category
        text description
        string image_url
        int max_profiles
        decimal price_profile
        decimal price_full
        int duration_days
        boolean is_active
        jsonb details
        timestamp created_at
    }

    ACCOUNTS {
        bigint id PK
        uuid uuid
        bigint service_id FK
        bigint vendor_id FK
        string email
        string password
        string sale_mode
        string plan
        decimal cost
        string source
        date purchased_at
        date renewal_date
        string status
        text notes
        timestamp created_at
    }

    PROFILES {
        bigint id PK
        uuid uuid
        bigint account_id FK
        string name
        string pin
        boolean is_owner
        string status
        timestamp created_at
    }

    SUBSCRIPTIONS {
        bigint id PK
        uuid uuid
        bigint client_id FK
        bigint profile_id FK
        bigint vendor_id FK
        bigint order_id FK
        date start_date
        date end_date
        date payment_due_date
        int months_paid
        string status
        text notes
        timestamp created_at
    }

    RESERVATIONS {
        bigint id PK
        bigint client_id FK
        bigint vendor_id FK
        string status
        decimal discount
        decimal total
        string receipt_url
        date expiration_date
        timestamp created_at
    }

    RESERVATION_DETAILS {
        bigint id PK
        bigint reservation_id FK
        bigint service_id FK
        int qty
        decimal unit_price
        decimal subtotal
    }

    ORDERS {
        bigint id PK
        bigint reservation_id FK
        bigint client_id FK
        bigint vendor_id FK
        string status
        string payment_method
        timestamp approved_at
        text notes
        timestamp created_at
    }

    NOTIFICATION_LOG {
        bigint id PK
        string entity_type
        bigint entity_id
        string stage
        timestamp sent_at
    }

    USERS ||--o| VENDORS : has
    USERS ||--o| CLIENTS : has

    VENDORS ||--o{ CLIENTS : manages
    VENDORS ||--o{ SERVICES : offers
    VENDORS ||--o{ ACCOUNTS : owns
    VENDORS ||--o{ SUBSCRIPTIONS : owns
    VENDORS ||--o{ RESERVATIONS : owns
    VENDORS ||--o{ ORDERS : owns

    SERVICES ||--o{ ACCOUNTS : provides
    SERVICES ||--o{ RESERVATION_DETAILS : reserved_as

    ACCOUNTS ||--o{ PROFILES : contains

    CLIENTS ||--o{ SUBSCRIPTIONS : has
    CLIENTS ||--o{ RESERVATIONS : creates
    CLIENTS ||--o{ ORDERS : places

    PROFILES ||--o{ SUBSCRIPTIONS : assigned_to

    RESERVATIONS ||--|{ RESERVATION_DETAILS : includes
    RESERVATIONS ||--o| ORDERS : converts_to

    ORDERS ||--o{ SUBSCRIPTIONS : generates
```

> [!NOTE]
> Campos de snapshot financiero en `subscriptions` (`service_id`, `sale_mode`, `price_sold`, `discount_applied`) están identificados como candidatos para EPIC-07/KPIs, pero no existen en el esquema backend vigente hasta EPIC-06. Para cuenta completa, `subscriptions.profile_id` apunta al perfil dueño (`is_owner = true`).
