# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 🔐 EPIC-01 — Autenticación y Roles
Esta épica cubre el registro de usuarios, el inicio de sesión y la seguridad basada en roles para los diferentes actores del sistema.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-012** | Registro de vendedor | Alta |
| **US-013** | Registro de cliente | Alta |
| **US-014** | Login | Alta |
| **US-015** | Control de acceso por rol | Alta |
| **US-016** | Cierre de sesión | Media |

---

### 🛠️ US-012 — Registro de vendedor
**Como** Super Admin, **necesito** poder registrar un nuevo vendedor en el sistema, **para** que pueda operar su propio negocio dentro de la plataforma.

> [!IMPORTANT]
> Decisión vigente ADR-09 revisada: el frontend crea primero la cuenta en Supabase Auth y envía el `externalId` al backend. El backend no genera contraseñas temporales ni devuelve credenciales iniciales; persiste `users` + `vendors` y registra el evento de bienvenida en `notification_log`.

#### ✅ Criterios de Aceptación
- [ ] Se crea un registro en la tabla `users` con el rol `vendedor`.
- [ ] Se crea un registro vinculado en la tabla `vendors`.
- [ ] Se registra una notificación de bienvenida sin credenciales generadas por backend.
- [ ] El acceso del vendedor queda activo de forma inmediata tras el registro.
- [ ] El request incluye `externalId` de Supabase Auth.

---

### 🛠️ US-013 — Registro de cliente
**Como** Cliente, **necesito** poder registrarme en la tienda de un vendedor específico, **para** poder realizar compras y consultar mis suscripciones.

> [!IMPORTANT]
> Decisión vigente ADR-09 revisada: la identidad del cliente se crea en Supabase Auth fuera del backend. El backend recibe `externalId`, vincula el cliente al vendedor y no administra contraseña ni sesión.

#### ✅ Criterios de Aceptación
- [ ] Se crea un registro en la tabla `users` con el rol `cliente`.
- [ ] Se crea un registro vinculado en la tabla `clients` asociado al `vendor_id` correspondiente de la tienda.
- [ ] El cliente recibe un correo electrónico de confirmación de registro.
- [ ] El request incluye `externalId` de Supabase Auth.
- [ ] La redirección al panel del cliente es responsabilidad del frontend.

---

### 🛠️ US-014 — Login
**Como** usuario autenticado, **necesito** poder iniciar sesión con mi correo y contraseña, **para** acceder al área que corresponde a mi rol.

> [!NOTE]
> Scope backend: validar JWT de Supabase y aplicar RBAC. Login, almacenamiento del token y redirecciones son responsabilidad de frontend.

#### ✅ Criterios de Aceptación
- [ ] El inicio de sesión redirige según el rol:
    - `super_admin` ➔ Panel de Administración Global.
    - `vendedor` ➔ Panel del Vendedor (su tienda).
    - `cliente` ➔ Panel del Cliente.
- [ ] El token emitido por el proveedor externo es validado correctamente por el backend.
- [ ] Una sesión inválida o expirada redirige automáticamente al usuario a la pantalla de login.

---

### 🛠️ US-015 — Control de acceso por rol
**Como** sistema, **necesito** que cada endpoint esté protegido según el rol requerido, **para** que ningún actor pueda acceder a recursos fuera de su alcance operativo.

#### ✅ Criterios de Aceptación
- [ ] Endpoints del panel del vendedor accesibles únicamente por el `vendedor` (propietario) y el `super_admin`.
- [ ] Endpoints del panel del cliente accesibles únicamente por el `cliente`.
- [ ] Endpoints de administración global accesibles únicamente por el `super_admin`.
- [ ] Endpoints públicos de la tienda (catálogo) accesibles sin necesidad de autenticación.
- [ ] Cualquier intento de acceso no autorizado retorna un código de error `403 Forbidden`.

---

### 🛠️ US-016 — Cierre de sesión
**Como** usuario autenticado, **necesito** poder cerrar sesión de forma segura, **para** proteger mi cuenta en dispositivos compartidos.

> [!NOTE]
> Scope backend: stateless. La invalidación de sesión se realiza en Supabase Auth y el token local se elimina en frontend.

#### ✅ Criterios de Aceptación
- [ ] La sesión es invalidada correctamente en el proveedor de identidad externo.
- [ ] El token de acceso es eliminado del almacenamiento local del cliente (browser/app).
- [ ] El sistema redirige a la pantalla de login tras un cierre de sesión exitoso.
