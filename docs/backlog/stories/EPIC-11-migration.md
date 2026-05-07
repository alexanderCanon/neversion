# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 📦 EPIC-11 — Migración Inicial
Esta épica define los procesos necesarios para cargar la información histórica del vendedor (clientes, servicios, inventario y suscripciones activas) al sistema para garantizar la continuidad del negocio.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-068** | Migrar clientes existentes | Alta |
| **US-069** | Migrar servicios existentes | Alta |
| **US-070** | Migrar cuentas existentes | Alta |
| **US-071** | Migrar suscripciones activas | Alta |
| **US-072** | Verificar estado post-migración | Media |

---

### 🛠️ US-068 — Migrar clientes existentes
**Como** Vendedor, **necesito** poder cargar mis clientes existentes al sistema, **para** no arrancar desde cero y mantener la continuidad operativa con mi base de datos actual.

#### ✅ Criterios de Aceptación
- [ ] El vendedor puede crear clientes de forma individual o mediante carga masiva (según implementación técnica).
- [ ] Validación de campos requeridos: `nombre`, `correo`.
- [ ] Soporte para campos opcionales: `teléfono`, `notas`.
- [ ] Por cada cliente migrado, el sistema genera automáticamente un registro en la tabla `users` con el rol `cliente`.
- [ ] El cliente recibe un correo electrónico con instrucciones para establecer su contraseña inicial.
- [ ] El sistema retorna un error `400 Bad Request` si se intenta migrar un correo que ya existe en la plataforma.

---

### 🛠️ US-069 — Migrar servicios existentes
**Como** Vendedor, **necesito** poder registrar mi catálogo de servicios actuales en el sistema, **para** tener la base comercial lista antes de abrir la tienda pública.

#### ✅ Criterios de Aceptación
- [ ] El vendedor puede registrar sus servicios manualmente desde el panel administrativo.
- [ ] Todos los campos del servicio (precios, duraciones, etc.) están disponibles para configuración desde el primer momento.
- [ ] Los servicios migrados se inicializan automáticamente en estado **activo**.

---

### 🛠️ US-070 — Migrar cuentas existentes
**Como** Vendedor, **necesito** poder registrar mi inventario técnico de cuentas maestras, **para** disponer de los recursos reales desde el primer día de operación.

#### ✅ Criterios de Aceptación
- [ ] El vendedor puede registrar sus cuentas de servicios manualmente.
- [ ] Validación de campos requeridos: `servicio`, `email`, `contraseña`, `modalidad_venta`, `fecha_renovación`, `costo_adquisición`.
- [ ] Las cuentas migradas inician automáticamente en estado `available`.

---

### 🛠️ US-071 — Migrar suscripciones activas
**Como** Vendedor, **necesito** registrar las suscripciones vigentes de mis clientes, **para** que el sistema refleje fielmente el estado actual del negocio y los próximos vencimientos reales.

#### ✅ Criterios de Aceptación
- [ ] El flujo permite crear suscripciones directamente (reutilizando la lógica de la **US-048**).
- [ ] Se respeta la **fecha de vencimiento real** del cliente (migrada desde sus registros actuales, ej: Excel).
- [ ] El perfil técnico vinculado transita automáticamente al estado `active`.
- [ ] El cliente solo recibe notificación de accesos si el vendedor marca explícitamente dicha opción.
- [ ] Las suscripciones migradas quedan marcadas como "creación manual" sin una orden de compra de origen.

---

### 🛠️ US-072 — Verificar estado post-migración
**Como** Vendedor, **necesito** validar la integridad de los datos cargados, **para** confirmar que la migración fue exitosa antes de arrancar operaciones públicas.

#### ✅ Criterios de Aceptación
- [ ] Los KPIs del dashboard (EPIC-10) reflejan correctamente los totales de clientes y suscripciones migradas.
- [ ] El reporte de próximos vencimientos muestra las fechas reales importadas durante la migración.
- [ ] La disponibilidad de inventario se descuenta correctamente según las suscripciones activas creadas.
- [ ] Validación técnica: No deben existir suscripciones en estado `active` vinculadas a perfiles que sigan en estado `available`.
    - [ ] 