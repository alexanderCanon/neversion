# B.3 Use Case Catalog

## Resumen de Casos de Uso

| ID | Caso de Uso | Actor Principal |
| :--- | :--- | :--- |
| **UC-01** | Gestión de Servicios | Vendedor |
| **UC-02** | Gestión de Cuentas | Vendedor |
| **UC-03** | Gestión de Perfiles | Vendedor |
| **UC-04** | Gestión de Clientes | Vendedor |
| **UC-05** | Registro y Autenticación | Cliente |
| **UC-06** | Creación de Orden | Cliente / Vendedor |
| **UC-07** | Subida de Comprobante | Cliente |
| **UC-08** | Aprobación de Pago | Vendedor |
| **UC-09** | Asignación de Accesos | Vendedor / Sistema |
| **UC-10** | Completar Orden | Vendedor |
| **UC-11** | Gestión de Suscripciones | Vendedor |
| **UC-12** | Panel del Cliente | Cliente |
| **UC-13** | Notificaciones | Sistema |
| **UC-14** | KPIs del Vendedor | Vendedor |
| **UC-15** | Migración Inicial | Vendedor / Super Admin |
| **UC-16** | Gestión de Vendedores | Super Admin |

---

## Mapa de Interacciones

```mermaid
usecaseDiagram
    actor "Vendedor" as V
    actor "Cliente" as C
    actor "Super Admin" as SA
    actor "Sistema" as S

    package "Operaciones Core" {
        V --> (UC-01 Gestión de Servicios)
        V --> (UC-02 Gestión de Cuentas)
        V --> (UC-03 Gestión de Perfiles)
        V --> (UC-11 Gestión de Suscripciones)
    }

    package "Ventas y Pagos" {
        C --> (UC-06 Creación de Orden)
        V --> (UC-06 Creación de Orden)
        C --> (UC-07 Subida de Comprobante)
        V --> (UC-08 Aprobación de Pago)
        V --> (UC-09 Asignación de Accesos)
        V --> (UC-10 Completar Orden)
    }

    package "Soporte y Admin" {
        C --> (UC-05 Registro / Login)
        C --> (UC-12 Panel Cliente)
        SA --> (UC-16 Gestión Vendedores)
        S --> (UC-13 Notificaciones)
    }
```

---

## Detalle de Casos de Uso

### UC-01 — Gestión de Servicios
- **Actor:** `Vendedor`
- **Descripción:**
  - Crear, editar y desactivar servicios (ej: Netflix, Spotify).
  - Definir precio por perfil y por cuenta completa.
  - Definir duración y cantidad máxima de perfiles por cuenta.

### UC-02 — Gestión de Cuentas
- **Actor:** `Vendedor`
- **Descripción:**
  - Crear, editar y administrar cuentas maestras de servicios.
  - Registrar costo de adquisición, fecha de compra y expiración.
  - Ver disponibilidad de perfiles por cuenta en tiempo real.

### UC-03 — Gestión de Perfiles
- **Actor:** `Vendedor`
- **Descripción:**
  - Generar perfiles individuales sobre una cuenta.
  - Cambiar estado (disponible, reservado, activo, vencido, bloqueado).
  - Revocar acceso de un perfil específico.

### UC-04 — Gestión de Clientes
- **Actor:** `Vendedor`
- **Descripción:**
  - Visualizar listado completo de clientes.
  - Crear clientes manualmente (ventas directas).
  - Ver detalle de suscripciones e historial de órdenes por cliente.

### UC-05 — Registro y Autenticación
- **Actor:** `Cliente`
- **Descripción:**
  - Registro con datos básicos (nombre, teléfono, correo).
  - Inicio de sesión seguro.
  - Edición de datos de perfil personal.

### UC-06 — Creación de Orden
- **Actor:** `Cliente / Vendedor`
- **Descripción:**
  - Selección de servicio y modalidad (perfil o cuenta completa).
  - Aplicación automática de descuentos por combo.
  - Confirmación de orden y generación de registro pendiente.

### UC-07 — Subida de Comprobante
- **Actor:** `Cliente`
- **Descripción:**
  - Cargar imagen o PDF del comprobante de pago vinculado a una orden.
  - Consultar el estado actual de la validación del comprobante.

### UC-08 — Aprobación de Pago
- **Actor:** `Vendedor`
- **Descripción:**
  - Revisión visual del comprobante subido por el cliente.
  - Aprobación o rechazo del pago con motivo.
  - Cambio automático de la orden a estado "Aprobado".

### UC-09 — Asignación de Perfil / Cuenta
- **Actor:** `Vendedor (Sugerencia del Sistema)`
- **Descripción:**
  - El sistema sugiere automáticamente el mejor perfil/cuenta disponible.
  - El vendedor valida y confirma la asignación final.
  - Disparador automático de envío de accesos.

### UC-10 — Completar Orden
- **Actor:** `Vendedor`
- **Descripción:**
  - Confirmación final de la entrega de credenciales.
  - Transición de la orden a estado "Completada".
  - Activación formal de la suscripción.

### UC-11 — Gestión de Suscripciones
- **Actor:** `Vendedor`
- **Descripción:**
  - Monitoreo de suscripciones activas y próximas a vencer.
  - Renovación manual de suscripciones.
  - Revocación de accesos por falta de pago o fin de servicio.

### UC-12 — Panel del Cliente
- **Actor:** `Cliente`
- **Descripción:**
  - Dashboard con suscripciones activas y fechas de corte.
  - Visualización directa de accesos (usuario/password/pin).
  - Historial de transacciones e inicio de procesos de renovación.

### UC-13 — Notificaciones por Correo
- **Actor:** `Sistema`
- **Descripción:**
  - Envío automático de correos en hitos clave: registro, orden, pago aprobado, entrega de accesos y recordatorios de vencimiento.

### UC-14 — KPIs del Vendedor
- **Actor:** `Vendedor`
- **Descripción:**
  - Métricas de vencimientos próximos (hoy, mañana, semana).
  - Ratio de ocupación de cuentas (disponibles vs ocupadas).
  - Control de ganancias y renovaciones logradas en el período.

### UC-15 — Migración Inicial
- **Actor:** `Vendedor / Super Admin`
- **Descripción:**
  - Carga masiva de datos existentes (clientes, suscripciones activas).
  - Sincronización de fechas de vencimiento reales pre-sistema.

### UC-16 — Gestión de Vendedores
- **Actor:** `Super Admin`
- **Descripción:**
  - Administración de inquilinos (Vendedores/Tiendas).
  - Activación/suspensión de acceso al sistema.
  - Auditoría global de métricas y métricas de plataforma.