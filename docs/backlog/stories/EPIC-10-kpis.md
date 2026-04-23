# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 📊 EPIC-10 — KPIs del Vendedor
Esta épica define los indicadores clave de desempeño (KPIs) que permiten al vendedor monitorear la salud operativa y financiera de su negocio en tiempo real.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-063** | Suscripciones por vencer | Alta |
| **US-064** | Disponibilidad de inventario | Alta |
| **US-065** | Conteo de clientes activos | Media |
| **US-066** | Renovaciones logradas | Media |
| **US-067** | Ganancia total del período | Alta |

---

### 🛠️ US-063 — Suscripciones por vencer
**Como** Vendedor, **necesito** ver de forma agrupada las suscripciones que están próximas a expirar, **para** priorizar mis gestiones de renovación y cobranza diarias.

#### ✅ Criterios de Aceptación
- [ ] El sistema presenta tres agrupaciones temporales: "Vencen hoy", "Vencen mañana" y "Vencen esta semana".
- [ ] Por cada registro se visualiza: cliente, servicio, perfil asignado y la fecha exacta de vencimiento.
- [ ] Únicamente se incluyen suscripciones en estado `active` o `suspended`.
- [ ] El reporte es exclusivo para las suscripciones del vendedor autenticado.

---

### 🛠️ US-064 — Disponibilidad de cuentas y perfiles
**Como** Vendedor, **necesito** ver un resumen consolidado de mi inventario disponible, **para** conocer mi capacidad de venta inmediata sin necesidad de navegar por cada cuenta.

#### ✅ Criterios de Aceptación
- [ ] El sistema muestra el total de perfiles disponibles vs. ocupados, desglosado por servicio.
- [ ] El sistema muestra el total de cuentas completas disponibles vs. ocupadas, desglosado por servicio.
- [ ] Los datos reflejados pertenecen exclusivamente al inventario del vendedor autenticado.

---

### 🛠️ US-065 — Clientes activos
**Como** Vendedor, **necesito** conocer el número total de clientes con servicios vigentes, **para** medir el tamaño real de mi base de clientes operativa y recurrente.

#### ✅ Criterios de Aceptación
- [ ] El sistema muestra un conteo único de clientes que poseen al menos una suscripción en estado `active`.
- [ ] Un cliente con múltiples suscripciones activas solo cuenta como uno en este KPI.
- [ ] El conteo considera únicamente los clientes vinculados al vendedor autenticado.

---

### 🛠️ US-066 — Renovaciones logradas
**Como** Vendedor, **necesito** ver el total de renovaciones procesadas con éxito, **para** medir la tasa de retención y la efectividad de mi gestión comercial.

#### ✅ Criterios de Aceptación
- [ ] El sistema muestra el conteo total de renovaciones completadas satisfactoriamente en el mes en curso.
- [ ] Solo se contabilizan las renovaciones de suscripciones pertenecientes al vendedor autenticado.

---

### 🛠️ US-067 — Ganancia total del período
**Como** Vendedor, **necesito** visualizar la ganancia monetaria generada en el período actual, **para** tener una visión clara de la rentabilidad bruta de mi negocio.

#### ✅ Criterios de Aceptación
- [ ] La ganancia se calcula como la sumatoria de (`price_sold` - `discount_applied`) de todas las suscripciones creadas o renovadas en el período.
- [ ] El período de tiempo predeterminado es el mes calendario en curso.
- [ ] El reporte solo incluye datos financieros de las suscripciones del vendedor autenticado.
- [ ] El valor monetario se expresa en **Quetzales (GTQ)**.