# 📋 Checklist de Validación UX e Integración - Neversion

Este documento sirve para validar el flujo completo del sistema tras la integración del Panel con el Backend (Sprint 1.5). Marcar con una `x` conforme se completen las pruebas.

---

## 🔐 1. Autenticación y Seguridad
- [ ] **Acceso Inicial:** Carga de `http://localhost:4200` y redirección al Login.
- [ ] **Login Supabase:** Introducir credenciales válidas.
- [ ] **Persistencia:** Al recargar la página (`F5`), la sesión se mantiene activa.
- [ ] **Interceptor JWT:** Verificar en la pestaña *Network* del navegador que las peticiones a `:8080/api/v1/*` llevan el header `Authorization: Bearer <token>`.

## 📦 2. Catálogo de Servicios
- [ ] **Crear Servicio:** Ir a `/services`, botón "Nuevo" y crear "Netflix Premium".
- [ ] **Listado:** El servicio aparece con su categoría (ej: `STREAMING`) y descripción.
- [ ] **Edición/Borrado:** Probar cambiar el nombre y eliminar un servicio de prueba.

## 💳 3. Cuentas y Perfiles (Infraestructura)
- [ ] **Añadir Cuenta:** En `/accounts`, crear cuenta para "Netflix Premium" con email y password.
- [ ] **Generación de Slots:** Al guardar, entrar al detalle y verificar que se crearon los perfiles (slots) según el `maxProfiles` del servicio.
- [ ] **Estado Inicial:** La cuenta debe aparecer como `AVAILABLE` o `ASSIGNED` según corresponda.

## 👥 4. Gestión de Clientes
- [ ] **Nuevo Cliente:** Crear a "Juan Pérez" en `/clients` con un número de teléfono válido.
- [ ] **Búsqueda:** Escribir "Juan" en el filtro y verificar que la lista se actualiza reactivamente (Signals).

## 📅 5. Reservas (Nuevo Módulo) 🚀
- [ ] **Listado de Reservas:** Acceder a `/reservations`. Debe cargar (aunque esté vacío inicialmente).
- [ ] **Crear Reserva:** (Simular/Crear) Una reserva para un servicio.
- [ ] **Navegación al Detalle:** Hacer clic en el ID (link azul) para ir a `/reservations/:id`.
- [ ] **Estados de Reserva:**
    - [ ] Ver botón "Cancelar" si está `PENDING`.
    - [ ] Visualizar el "Comprobante de Pago" si la reserva tiene `receiptUrl`.
- [ ] **Asignación de Cliente:** Si la reserva no tiene cliente, usar el selector en el detalle para asignarle a "Juan Pérez".

## 🧾 6. Órdenes y Validación de Pagos
- [ ] **Validar Pago:** En una reserva con estado `UPLOADED`, escribir una nota y presionar "Validar y Crear Orden".
- [ ] **Flujo Automático:** Verificar que la reserva pasa a `VALIDATED` y el botón de validación desaparece.
- [ ] **Módulo Órdenes:** Ir a `/orders` y verificar que existe una nueva orden vinculada al ID de la reserva.

## 📝 7. Suscripciones (Ciclo Final)
- [ ] **Crear Suscripción:** En `/subscriptions`, asignar un perfil de Netflix a Juan Pérez.
- [ ] **Cierre de Ciclo:** La suscripción debe aparecer como `ACTIVE`.
- [ ] **Corrección Endpoints:** Probar el botón **"Cancelar"**. Debe llamar a `/api/v1/subscriptions/{id}/cancel` (no `/terminate`).
- [ ] **Filtros:** Filtrar por estado `CANCELLED` y verificar que la lista se actualiza.

## 📊 8. Dashboard Maestro (Consistencia de Datos)
- [ ] **Resumen de Productos:** Volver a `/dashboard`. El servicio "Netflix" debe reflejar los cambios:
    - [ ] `totalAccounts` actualizado.
    - [ ] Al expandir el servicio, ver la cuenta con sus perfiles ocupados/disponibles.
- [ ] **Lazy Loading:** Los perfiles deben cargarse solo al expandir la fila de la cuenta.

---

### 🚨 Notas de Error (Si algo falla, anótalo aquí):
*   *Módulo:* 
*   *Error observado:* 
*   *Consola (F12):* 
