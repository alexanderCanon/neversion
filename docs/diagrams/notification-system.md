# Sistema de Notificaciones (EPIC-08)

Este diagrama describe la arquitectura del sistema de notificaciones por correo y el flujo de procesamiento.

## Arquitectura del Worker

```mermaid
graph TD
    subgraph Sources ["Fuentes de Notificación"]
        S1["RegisterVendorService"]
        S2["RegisterClientService"]
        S3["ClientService"]
        S4["ValidateReservationService"]
        S5["RejectReservationService"]
        S6["UploadReceiptService"]
        S7["DeliverAccessService"]
        S8["SuggestAssignmentService"]
        S9["RevokeSubscriptionService"]
        S10["RenewSubscriptionService"]
        S11["DetectExpiredSubscriptionsService"]
        S12["ChangeOrderStatusService"]
        S13["SendRenewalRemindersService"]
    end

    subgraph Core ["Infraestructura de Notificaciones"]
        NLP["NotificationLogPort<br/>record(type, email, payload, entityType, entityId, stage)"]
        DB[(notification_log<br/>status: pending)]
        Worker["NotificationWorkerService<br/>processNextBatch()"]
        Resolver["NotificationTemplateResolver<br/>13 tipos → TemplateSpec"]
        Sender["EmailSenderPort<br/>ResendEmailAdapter"]
    end

    subgraph Triggers ["Disparadores"]
        Scheduler["NotificationScheduler<br/>cada 30s"]
        Manual["POST /notifications/process<br/>SUPER_ADMIN"]
        RenewalCron["RenewalReminderScheduler<br/>diario 08:00"]
    end

    subgraph External ["Servicios Externos"]
        Resend["Resend API"]
        Email["📧 Correo del Cliente"]
    end

    S1 & S2 & S3 & S4 & S5 & S6 & S7 & S8 & S9 & S10 & S11 & S12 --> NLP
    S13 --> NLP
    NLP --> DB
    Scheduler --> Worker
    Manual --> Worker
    RenewalCron --> S13
    Worker --> DB
    Worker --> Resolver
    Resolver --> Worker
    Worker --> Sender
    Sender --> Resend
    Resend --> Email
```

## Flujo de Procesamiento

```mermaid
sequenceDiagram
    autonumber
    participant Svc as Servicio de Negocio
    participant NLP as NotificationLogPort
    participant DB as notification_log
    participant Sch as Scheduler (30s)
    participant Worker as NotificationWorkerService
    participant Tmpl as TemplateResolver
    participant Email as ResendEmailAdapter
    participant Resend as Resend API

    Svc->>NLP: record(type, email, payload, entityType, entityId, stage)
    NLP->>DB: INSERT (status=PENDING)

    loop Cada 30 segundos
        Sch->>Worker: processNextBatch()
        Worker->>DB: SELECT WHERE status=PENDING LIMIT 50
        Worker->>Tmpl: resolve(type)
        alt Template exists
            Tmpl-->>Worker: TemplateSpec(template, subject)
            Worker->>Email: send(to, subject, htmlBody)
            Email->>Resend: POST /emails
            alt Envío exitoso
                Resend-->>Email: 200 OK
                Worker->>DB: UPDATE status=SENT, processed_at=now()
            else Error de envío
                Resend-->>Email: Error
                Worker->>DB: UPDATE status=FAILED, error_message
            end
        else Template skipped
            Tmpl-->>Worker: TemplateSpec(skipped=true)
            Worker->>DB: UPDATE status=SENT, processed_at=now()
        end
    end
```

## Deduplicación

```mermaid
graph LR
    subgraph Dedup ["Índice Único: idx_notif_dedup"]
        ET["entity_type<br/>(subscription, order, client, vendor)"]
        EI["entity_id<br/>(Long PK)"]
        ST["stage<br/>(welcome, approved, reminder_7d, etc.)"]
    end

    ET --- EI --- ST
    ST -->|Previene| DUP["Notificaciones Duplicadas"]
```

## Tipos de Notificación

| Tipo | entity_type | stage | Template | Disparador |
|:---|:---|:---|:---|:---|
| `VENDOR_WELCOME` | vendor | welcome | vendor-welcome | Registro vendor |
| `CLIENT_REGISTRATION` | client | welcome | client-registration | Registro cliente (auth) |
| `CLIENT_WELCOME` | client | welcome | client-welcome | Registro cliente (manual) |
| `PAYMENT_APPROVED` | order | approved | payment-approved | Validación de reserva |
| `RECEIPT_REJECTED` | order | rejected | receipt-rejected | Rechazo de comprobante |
| `RECEIPT_UPLOADED` | order | receipt_uploaded | receipt-uploaded | Subida de comprobante |
| `ACCESS_DELIVERED` | subscription | access_delivered | access-delivered | Asignación confirmada |
| `NO_INVENTORY_ALERT` | vendor | no_inventory | no-inventory-alert | Sin perfiles disponibles |
| `ACCESS_REVOKED` | subscription | revoked | access-revoked | Revocación manual |
| `SUBSCRIPTION_RENEWED` | subscription | renewed | subscription-renewed | Renovación exitosa |
| `SUBSCRIPTIONS_EXPIRED_DAILY` | vendor | expired_daily | expired-daily | Cron diario 02:00 |
| `SUBSCRIPTION_EXPIRED` | subscription | due | subscription-expired | Cron diario 02:00 |
| `RENEWAL_REMINDER_7D` | subscription | reminder_7d | renewal-reminder | Cron diario 08:00 |
| `RENEWAL_REMINDER_3D` | subscription | reminder_3d | renewal-reminder | Cron diario 08:00 |
| `RENEWAL_REMINDER_1D` | subscription | reminder_1d | renewal-reminder | Cron diario 08:00 |

## Configuración

```yaml
# application.yaml
neversion:
  email:
    from: ${NEVERSION_EMAIL_FROM}
    resend-api-key: ${RESEND_API_KEY}
  cron:
    notification-worker:
      enabled: ${NEVERSION_CRON_NOTIFICATION_WORKER_ENABLED:false}
      interval-ms: 30000
    renewal-reminders:
      enabled: false
      cron: "0 0 8 * * *"
    subscription-expiry:
      enabled: false
      cron: "0 0 2 * * *"
```
