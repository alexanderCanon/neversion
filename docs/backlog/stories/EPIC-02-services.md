# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 📺 EPIC-02 — Gestión de Servicios
Esta épica abarca la administración del catálogo de servicios digitales por parte del vendedor y su visualización en la tienda pública.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-017** | Crear servicio | Alta |
| **US-018** | Editar servicio | Media |
| **US-019** | Activar / desactivar servicio | Media |
| **US-020** | Listar servicios (Panel Vendedor) | Alta |
| **US-021** | Listar servicios (Tienda) | Alta |

---

### 🛠️ US-017 — Crear servicio
**Como** Vendedor, **necesito** poder crear un nuevo servicio en mi catálogo, **para** ofrecerlo a mis clientes en la tienda.

#### ✅ Criterios de Aceptación
- [ ] Se crea un registro en la tabla `services` vinculado al `vendor_id` del vendedor autenticado.
- [ ] Validación de campos requeridos: `nombre`, `categoría`, `precio_perfil`, `precio_completo`, `duración_días`, `cantidad_max_perfiles`.
- [ ] Campos opcionales soportados: `descripción`, `imagen`.
- [ ] El servicio se crea inicialmente en estado **activo**.
- [ ] Si faltan campos requeridos, el sistema retorna un error `400 Bad Request` con el detalle correspondiente.

---

### 🛠️ US-018 — Editar servicio
**Como** Vendedor, **necesito** poder editar un servicio existente, **para** mantener actualizado mi catálogo.

#### ✅ Criterios de Aceptación
- [ ] Únicamente el vendedor propietario del servicio tiene permisos para editarlo.
- [ ] Todos los campos funcionales son editables, excepto los identificadores técnicos (`id`, `uuid`).
- [ ] Los cambios de precio no afectan retroactivamente a las suscripciones activas existentes.
- [ ] Si un vendedor intenta editar un servicio ajeno, el sistema retorna un error `403 Forbidden`.

---

### 🛠️ US-019 — Activar / desactivar servicio
**Como** Vendedor, **necesito** poder activar o desactivar un servicio, **para** controlar la disponibilidad en mi tienda sin necesidad de eliminar el registro.

#### ✅ Criterios de Aceptación
- [ ] Un servicio en estado **inactivo** no debe aparecer listado en la tienda pública.
- [ ] El servicio inactivo sigue siendo visible y gestionable desde el panel privado del vendedor.
- [ ] Desactivar un servicio no afecta el ciclo de vida ni el acceso de las suscripciones activas vinculadas a él.

---

### 🛠️ US-020 — Listar servicios en panel del vendedor
**Como** Vendedor, **necesito** ver el listado completo de mis servicios con su estado y disponibilidad, **para** administrar mi catálogo de manera eficiente.

#### ✅ Criterios de Aceptación
- [ ] El listado muestra todos los servicios (activos e inactivos) del vendedor autenticado.
- [ ] Se visualiza la información clave: nombre, categoría, precios, duración, estado y capacidad de perfiles.
- [ ] El sistema permite filtrar la lista por `categoría` y `estado`.

---

### 🛠️ US-021 — Listar servicios en tienda
**Como** Cliente, **necesito** ver los servicios disponibles en la tienda del vendedor, **para** elegir qué producto quiero comprar.

#### ✅ Criterios de Aceptación
- [ ] El catálogo solo muestra servicios en estado **activo** del vendedor correspondiente.
- [ ] Cada item muestra: nombre, descripción, imagen (si existe), precio por perfil y precio por cuenta completa.
- [ ] El catálogo es de acceso público (no requiere autenticación previa).