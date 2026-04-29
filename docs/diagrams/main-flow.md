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
```

## Estados de la Orden
*   **PENDING:** Reservación creada, esperando comprobante.
*   **UPLOADED:** Comprobante enviado, esperando revisión del vendedor.
*   **VALIDATED:** Pago aprobado, lista para asignar perfiles.
*   **COMPLETED:** Perfiles asignados y credenciales entregadas.
*   **REJECTED / CANCELLED:** Flujos de excepción por pago inválido o expiración.
