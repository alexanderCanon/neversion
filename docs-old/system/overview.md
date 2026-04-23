# Neversion — Visión General del Sistema

> **Fuente de verdad:** Este documento consolida el contexto de negocio, el dominio y las reglas transversales del sistema Neversion.

---

## 1. Qué es Neversion

Neversion es una plataforma de comercio digital enfocada en facilitar el acceso a servicios digitales (streaming, recargas, gift cards). Opera como intermediario entre proveedores mayoristas y consumidores finales, especialmente para usuarios con barreras de pago tradicionales.

**Mercado objetivo:** Personas de 17–35 años en Guatemala con acceso limitado a medios de pago online.

---

## 2. Propósito del Sistema

El sistema administra el ciclo completo de vida de una suscripción digital:

1. **Catálogo** — Servicios disponibles (Netflix, Spotify, etc.)
2. **Inventario** — Cuentas maestras compradas al mayorista
3. **Asignación** — Perfiles de cuenta asignados a clientes
4. **Facturación** — Seguimiento de fechas de pago y renovación
5. **Tienda** — Flujo de reserva y pago del cliente final (Fase 3)

---

## 3. Dominio — Glosario de Entidades

| Entidad | Tabla | Descripción |
|---|---|---|
| **Servicio** | `services` | Plataforma digital ofrecida (ej. Netflix, Disney+). Anchor point del catálogo. |
| **Cuenta** | `accounts` | Credencial maestra comprada al proveedor mayorista. Tiene `email`, `password`, `renewal_date`. |
| **Perfil** | `profiles` | Subdivisión de una Cuenta (antes "Slot"). Representa un espacio de acceso individual dentro de una plataforma. |
| **Cliente** | `clients` | Consumidor final que paga por un Perfil. Reemplaza `users_guests` (Sprint 1.5). |
| **Suscripción** | `subscriptions` | Vínculo activo entre un Cliente y un Perfil, con fechas de pago. |
| **Reservación** | `reservations` | Estado temporal de checkout. Expira en 60 minutos. |
| **Orden** | `orders` | Registro persistente creado al validar el pago de una Reservación. |
| **Notification Log** | `notification_log` | Auditoría de eventos de automatización (n8n) para renovaciones. |

---

## 4. Jerarquía de Dominio

```
Servicio (Netflix)
  └── Cuenta (credencial maestro comprada)
        └── Perfil (slot individual)
              └── Suscripción (link cliente ↔ perfil + fechas de pago)
                    └── Cliente (consumidor final)

Cliente
  └── Reservación (checkout temporal)
        └── Orden (al validar el pago)
```

---

## 5. Contexto de Negocio

- **Operación actual:** Semi-manual. El Admin compra cuentas al mayorista y las asigna a clientes vía panel administrativo.
- **Automatización:** Notificaciones de renovación vía n8n + `notification_log` (WhatsApp/Email a 7 días, 3 días, vencido).
- **Fase actual (Sprint 1.5):** Panel administrativo operativo + API backend completa.
- **Fase siguiente (Sprint 2+):** Tienda online pública, pago automatizado, portal de cliente.

---

## 6. Fundador

Alexander Canon, estudiante de Ingeniería en Sistemas con +5 años de experiencia operando en el espacio de servicios digitales. La marca evolucionó de "New Experience" a "Neversion" para reflejar una identidad más escalable y técnica.

---

## Cuándo leer este archivo

- Primera exploración del repositorio
- Entender qué hace el negocio y por qué existen ciertas decisiones
- Mapear entidades de dominio antes de leer módulos específicos

**Continuar en:** [`architecture.md`](architecture.md) para la arquitectura técnica, o ir directamente a un módulo en [`../modules/`](../modules/).
