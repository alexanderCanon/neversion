# Flujo Crítico: Compra, Validación y Entrega

Este diagrama de secuencia detalla el proceso completo desde que un cliente inicia una compra hasta que recibe sus credenciales y las notificaciones asociadas.

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente (Store)
    participant V as Vendedor (Panel)
    participant B as API (Backend)
    participant W as Notification Worker
    participant R as Resend (Email)

    Note over C, B: EPIC-05: Proceso de Venta
    C->>B: Crear Reservación (POST /reservations)
    B-->>C: 201 Created (ID Reserva)
    C->>B: Subir Comprobante (PUT /receipt)
    B->>B: record(RECEIPT_UPLOADED)

    V->>B: Validar Reserva (PATCH /validate)
    B->>B: Crear Orden de Venta
    B->>B: record(PAYMENT_APPROVED)

    Note over V, B: EPIC-06: Asignación y Entrega
    V->>B: Sugerir Perfil (GET /assignments/suggest)
    B->>B: Buscar disponibilidad (Inventory)
    B-->>V: DTO: Perfil Sugerido
    
    V->>B: Confirmar Asignación (POST /assignments/confirm)
    B->>B: Cambiar Perfil a ACTIVE
    B->>B: Crear Suscripción (Dates calc)
    B->>B: Marcar Orden COMPLETED
    B->>B: record(ACCESS_DELIVERED)
    
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
    B->>B: record(SUBSCRIPTION_RENEWED)
    V->>B: Revocar acceso (PUT /subscriptions/{id}/cancel)
    B->>B: Cancelar suscripción y liberar inventario
    B->>B: record(ACCESS_REVOKED)
    B->>B: Scheduler diario 02:00 detecta vencidas
    B->>B: Suspender suscripciones y expirar inventario
    B->>B: record(SUBSCRIPTIONS_EXPIRED_DAILY + SUBSCRIPTION_EXPIRED)
    V->>B: Crear suscripción manual (POST /subscriptions)
    B->>B: Validar ownership, disponibilidad y modalidad
    opt sendNotification=true
        B->>B: record(ACCESS_DELIVERED)
    end

    Note over C, B: EPIC-09 US-061: Renovación solicitada por cliente
    C->>B: Solicitar renovación (POST /reservations/renew)
    B->>B: Validar ownership del cliente + suscripción ACTIVE/SUSPENDED
    B-->>C: 201 Created (Reservación de Renovación)
    C->>B: Subir Comprobante (PUT /reservations/{id}/receipt)
    V->>B: Aprobar Comprobante (PUT /reservations/{id}/validate)
    B->>B: Crear Orden VALIDATED
    B->>B: Renovar Suscripción existente
    B->>B: Marcar Orden COMPLETED
    B->>B: record(SUBSCRIPTION_RENEWED)

    Note over W, R: EPIC-08: Entrega de Correos
    loop Cada 30 segundos
        W->>B: Fetch PENDING notifications (batch 50)
        W->>W: Resolver template + subject
        W->>R: Send email via Resend API
        R-->>C: 📧 Correo al cliente
        W->>B: Update status SENT/FAILED
    end

    Note over B, W: EPIC-08 US-054: Recordatorios
    B->>B: Scheduler diario 08:00
    B->>B: Buscar suscripciones venciendo en 7d/3d/1d
    B->>B: record(RENEWAL_REMINDER_7D/3D/1D) con dedup
```

## Estados de la Orden
*   **PENDING:** Reservación creada, esperando comprobante.
*   **UPLOADED:** Comprobante enviado, esperando revisión del vendedor.
*   **VALIDATED:** Pago aprobado, lista para asignar perfiles.
*   **COMPLETED:** Perfiles asignados y credenciales entregadas.
*   **REJECTED / CANCELLED:** Flujos de excepción por pago inválido o expiración.
