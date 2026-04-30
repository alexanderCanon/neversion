# KPIs del Vendedor (EPIC-10)

Este diagrama resume los endpoints backend de KPIs del vendedor autenticado. Todos resuelven el `vendorId` desde el JWT y no aceptan identificadores de vendedor en el request.

```mermaid
flowchart TD
    V["Vendedor autenticado"] --> API["DashboardController /api/v1/dashboard/kpis"]
    API --> AUTH["Resolver vendor desde JWT"]
    AUTH --> E["US-063 GET /expiring-subscriptions"]
    AUTH --> I["US-064 GET /inventory-availability"]
    AUTH --> C["US-065 GET /active-clients"]
    AUTH --> R["US-066 GET /successful-renewals"]
    AUTH --> G["US-067 GET /gross-profit"]

    E --> EQ["subscriptions ACTIVE/SUSPENDED con payment_due_date hoy, mañana y semana"]
    I --> IQ["services + accounts + profiles por disponibilidad vendible"]
    C --> CQ["COUNT DISTINCT subscriptions.client_id con status ACTIVE"]
    R --> RQ["orders COMPLETED de reservaciones con renewal_subscription_id en mes actual"]
    G --> GQ["subscriptions creadas + renovaciones exitosas del mes actual"]

    EQ --> ER["DTO: today, tomorrow, thisWeek"]
    IQ --> IR["DTO: disponibilidad por servicio"]
    CQ --> CR["DTO: activeClientsCount"]
    RQ --> RR["DTO: successfulRenewalsCount"]
    GQ --> GR["DTO: grossProfit, currency GTQ"]
```

## Reglas

1. Solo `ROLE_VENDOR` puede consultar `/api/v1/dashboard/kpis/**`.
2. `SUPER_ADMIN` no accede a estos KPIs porque el alcance confirmado para EPIC-10 es vendedor autenticado.
3. El período financiero predeterminado es el mes calendario actual calculado en backend.
4. La ganancia bruta usa snapshots financieros de `subscriptions`: `price_sold - discount_applied`.
