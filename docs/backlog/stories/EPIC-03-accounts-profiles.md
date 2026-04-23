# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 🔑 EPIC-03 — Gestión de Cuentas y Perfiles
Esta épica se enfoca en la administración técnica y operativa de las cuentas maestras de servicios y los perfiles individuales que se derivan de ellas.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-022** | Crear cuenta | Alta |
| **US-023** | Editar cuenta | Media |
| **US-024** | Ver disponibilidad de cuentas | Alta |
| **US-025** | Generar perfiles sobre una cuenta | Alta |
| **US-026** | Editar perfil | Media |
| **US-027** | Cambiar estado de perfil (Manual) | Media |
| **US-028** | Detalle de cuenta y perfiles | Alta |

---

### 🛠️ US-022 — Crear cuenta
**Como** Vendedor, **necesito** poder registrar una nueva cuenta en el sistema, **para** tener control sobre las credenciales que adquiero y administro.

#### ✅ Criterios de Aceptación
- [ ] Se crea un registro en la tabla `accounts` vinculado al `vendor_id` del vendedor autenticado.
- [ ] Validación de campos requeridos: `servicio`, `email`, `contraseña`, `modalidad_venta`, `fecha_renovación`, `costo_adquisición`.
- [ ] Soporte para campos opcionales: `plan`, `fuente`, `fecha_compra`, `notas`.
- [ ] La cuenta inicia automáticamente en estado `available`.
- [ ] Si faltan campos requeridos, el sistema retorna un error `400 Bad Request` con el detalle correspondiente.

---

### 🛠️ US-023 — Editar cuenta
**Como** Vendedor, **necesito** poder editar una cuenta existente, **para** mantener sus datos operativos actualizados.

#### ✅ Criterios de Aceptación
- [ ] Únicamente el vendedor propietario puede editar sus propias cuentas.
- [ ] Todos los campos operativos son editables, excepto los identificadores técnicos (`id`, `uuid`).
- [ ] Si un vendedor intenta editar una cuenta ajena, el sistema retorna un error `403 Forbidden`.

---

### 🛠️ US-024 — Ver disponibilidad de cuentas
**Como** Vendedor, **necesito** ver el estado y disponibilidad de mis cuentas y sus perfiles, **para** saber qué puedo asignar a mis clientes en cualquier momento.

#### ✅ Criterios de Aceptación
- [ ] El listado muestra todas las cuentas del vendedor con su estado actual.
- [ ] Por cada cuenta se visualiza: servicio, email, modalidad, estado, conteo de perfiles (disponibles vs ocupados) y fecha de renovación.
- [ ] El sistema permite filtrar la lista por `servicio` y `estado`.

---

### 🛠️ US-025 — Generar perfiles sobre una cuenta
**Como** Vendedor, **necesito** poder generar perfiles sobre una cuenta, **para** disponer de unidades vendibles listas para ser asignadas.

#### ✅ Criterios de Aceptación
- [ ] Se crean registros en la tabla `profiles` vinculados a la cuenta maestra seleccionada.
- [ ] El sistema valida que la cantidad de perfiles generados no supere el `max_profiles` definido en la configuración del servicio.
- [ ] Cada perfil nuevo inicia automáticamente en estado `available`.
- [ ] Soporte para campos opcionales por perfil: `nombre`, `PIN`, `is_owner`.
- [ ] Retorna un error `400 Bad Request` si se intenta superar el límite de perfiles permitidos.

---

### 🛠️ US-026 — Editar perfil
**Como** Vendedor, **necesito** poder editar la información de un perfil existente, **para** mantener sus datos actualizados.

#### ✅ Criterios de Aceptación
- [ ] Los campos editables son: `nombre`, `PIN`, `is_owner`.
- [ ] El estado del perfil no es editable directamente desde este flujo (es controlado por eventos de negocio).
- [ ] El sistema retorna un error `403 Forbidden` si el vendedor no es el propietario de la cuenta a la que pertenece el perfil.

---

### 🛠️ US-027 — Cambiar estado de perfil manualmente
**Como** Vendedor, **necesito** poder bloquear o desbloquear un perfil manualmente, **para** gestionar casos excepcionales de la operación diaria.

#### ✅ Criterios de Aceptación
- [ ] El vendedor puede cambiar el estado a `blocked` o `available` de forma manual.
- [ ] El sistema **prohíbe** cambiar manualmente a los estados `active`, `reserved` o `expired` (controlados exclusivamente por lógica de negocio).
- [ ] El sistema retorna un error `400 Bad Request` si se intenta realizar una transición de estado no permitida manualmente.

---

### 🛠️ US-028 — Ver detalle de cuenta con sus perfiles
**Como** Vendedor, **necesito** ver el detalle completo de una cuenta incluyendo todos sus perfiles y estados, **para** tener una visibilidad operativa total.

#### ✅ Criterios de Aceptación
- [ ] Se muestran todos los datos maestros de la cuenta.
- [ ] Se listan todos los perfiles asociados con su nombre, PIN, estado y el enlace a la suscripción activa vinculada (si existe).
- [ ] Se incluye un resumen visual de la cuenta: total de perfiles, cantidad disponibles, ocupados y bloqueados.