# Flujo Crítico: Compra, Validación y Entrega

Este diagrama de secuencia detalla el proceso completo desde que un cliente inicia una compra hasta que recibe sus credenciales (completado en EPIC-06).

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente (Store)
    participant V as Vendedor (Panel)
    participant B as API (Backend)
    participant N as n8n (Notificaciones)

    Note over C, B: EPIC-05: Proceso de Venta
    C->>B: Crear Reservación (POST /reservations)
    B-->>C: 201 Created (ID Reserva)
    C->>B: Subir Comprobante (PUT /receipt)
    B-->>N: Evento: RECEIPT_UPLOADED
    N-->>V: Notificación WhatsApp: "Nuevo pago"

    V->>B: Validar Reserva (PATCH /validate)
    B->>B: Crear Orden de Venta
    B-->>N: Evento: ORDER_VALIDATED
    N-->>C: Notificación: "Pago aprobado"

    Note over V, B: EPIC-06: Asignación y Entrega
    V->>B: Sugerir Perfil (GET /assignments/suggest)
    B->>B: Buscar disponibilidad (Inventory)
    B-->>V: DTO: Perfil Sugerido
    
    V->>B: Confirmar Asignación (POST /assignments/confirm)
    B->>B: Cambiar Perfil a ACTIVE
    B->>B: Crear Suscripción (Dates calc)
    B->>B: Marcar Orden COMPLETED
    B-->>N: Evento: ACCESS_DELIVERED
    
    Note over C, B: US-041: Consulta de Credenciales
    C->>B: Ver mis accesos (GET /clients/me/accesses)
    B-->>C: DTO: Email, Password, PIN, ServiceName

    Note over V, B: EPIC-07: Gestión de Suscripciones
    V->>B: Listar suscripciones (GET /subscriptions/vendor/{vendorUuid})
    B->>B: Validar ownership del Vendor
    B->>B: Filtrar por servicio/estado y ordenar por vencimiento
    B-->>V: DTO: Cliente, Servicio, Perfil, Vencimiento, Estado
    V->>B: Ver detalle (GET /subscriptions/{id})
    B-->>V: DTO: Origen comercial, snapshots, cliente, perfil, cuenta
    V->>B: Renovar suscripción (PUT /subscriptions/{id}/renew)
    B->>B: Aplicar BR-07 y restaurar perfiles/cuenta
    B-->>N: Evento: SUBSCRIPTION_RENEWED
    V->>B: Revocar acceso (PUT /subscriptions/{id}/cancel)
    B->>B: Cancelar suscripción y liberar inventario
    B-->>N: Evento: ACCESS_REVOKED
    B->>B: Scheduler diario 02:00 detecta vencidas
    B->>B: Suspender suscripciones y expirar inventario
    B-->>N: Evento: SUBSCRIPTIONS_EXPIRED_DAILY por vendedor
    V->>B: Crear suscripción manual (POST /subscriptions)
    B->>B: Validar ownership, disponibilidad y modalidad
    opt sendNotification=true
        B-->>N: Evento: ACCESS_DELIVERED
    end
```

## Estados de la Orden
*   **PENDING:** Reservación creada, esperando comprobante.
*   **UPLOADED:** Comprobante enviado, esperando revisión del vendedor.
*   **VALIDATED:** Pago aprobado, lista para asignar perfiles.
*   **COMPLETED:** Perfiles asignados y credenciales entregadas.
*   **REJECTED / CANCELLED:** Flujos de excepción por pago inválido o expiración.
