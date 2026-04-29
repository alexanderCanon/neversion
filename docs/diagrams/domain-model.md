# Modelo de Dominio e Inventario

Este diagrama describe las relaciones jerárquicas del inventario y el ciclo de vida de los recursos (EPIC-02 y EPIC-03).

```mermaid
classDiagram
    class Vendor {
        +UUID uuid
        +String storeName
    }

    class Service {
        +UUID uuid
        +String name
        +CategoryType category
        +Integer durationDays
        +Double priceProfile
        +Double priceComplete
    }

    class Account {
        +UUID uuid
        +String email
        +String password
        +SaleMode saleMode
        +AccountStatus status
    }

    class Profile {
        +UUID uuid
        +String name
        +String pin
        +ProfileStatus status
        +Boolean isOwner
    }

    class Subscription {
        +UUID uuid
        +LocalDate startDate
        +LocalDate endDate
        +SubStatus status
    }

    class Client {
        +UUID uuid
        +String name
        +String email
    }

    Vendor "1" -- "*" Service : ofrece
    Service "1" -- "*" Account : contiene
    Account "1" -- "*" Profile : se divide en
    
    Client "1" -- "*" Subscription : posee
    Subscription "*" -- "1" Service : referencia
    Subscription "1" -- "1" Profile : entrega acceso a
```

## Reglas de Negocio Clave
1.  **Multi-tenancy:** Casi todas las entidades (`Service`, `Account`, `Client`, `Subscription`) pertenecen a un `Vendor`. El aislamiento se garantiza por `vendorId`.
2.  **Jerarquía de Inventario:** Un `Service` (ej: Netflix) tiene múltiples `Account` (cuentas maestras). Cada cuenta tiene un cupo limitado de `Profile` (perfiles individuales).
3.  **Estados de Perfil:**
    *   `AVAILABLE`: Listo para ser asignado.
    *   `ACTIVE`: Asignado a una suscripción vigente.
    *   `BLOCKED`: Bloqueado manualmente por el vendedor.
    *   `EXPIRED`: La suscripción asociada ha vencido.
