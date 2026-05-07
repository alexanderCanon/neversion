# Ciclo de Vida de la Suscripción (EPIC-07)

Este diagrama describe los estados y transiciones de una suscripción y sus recursos vinculados.

```mermaid
stateDiagram-v2
    [*] --> PENDING: Creación (Reserva)
    PENDING --> VALIDATED: Pago Subido y Validado
    VALIDATED --> ACTIVE: Perfil Asignado (EPIC-06)
    
    state "Suscripción Vigente" as Vigente {
        ACTIVE --> SUSPENDED: Fecha Vencimiento Alcanzada (Auto)
        SUSPENDED --> ACTIVE: Renovación (Pago 1-2 días tarde)
        ACTIVE --> ACTIVE: Renovación (Pago a tiempo)
    }
    
    Vigente --> CANCELLED: Revocación Manual
    SUSPENDED --> CANCELLED: Revocación por Falta de Pago
    
    note right of ACTIVE
        Perfil: ACTIVE
    end note
    
    note right of SUSPENDED
        Perfil: EXPIRED
    end note
    
    note right of CANCELLED
        Perfil: AVAILABLE
    end note
```

## Reglas de Transición (BR-07)
1.  **Renovación Temprana/A Tiempo:** La nueva fecha de vencimiento se calcula sumando la duración del servicio a la fecha de vencimiento **original**.
2.  **Renovación Tardía (>= 3 días):** La nueva fecha de vencimiento se calcula desde la **fecha de pago actual**.
3.  **Detección de Vencimiento:** Proceso automático diario que cambia `ACTIVE -> SUSPENDED` y marca el perfil como `EXPIRED`.
4.  **Revocación:** Libera el perfil inmediatamente marcándolo como `AVAILABLE` para que pueda ser re-vendido.
