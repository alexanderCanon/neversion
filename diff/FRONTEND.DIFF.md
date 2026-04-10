Refactorización del Frontend (Sprint 1.5) - Panel Administrativo
================================================================

Este documento resume los cambios estructurales, lógicos y estéticos realizados en el frontend (proyecto Angular `panel`) durante el Sprint 1.5. Estos cambios alinean la interfaz con la nueva arquitectura Hexagonal y el Data Model consolidado del Backend (`api`).

## 1. Migración y Renombrado de Dominios Módulares (Routing & Navigation)

Antes de la refactorización, el proyecto utilizaba terminología confusa que arrastraba conceptos estáticos o e-commerce tradicionales.
Todos los enrutamientos en `app.routes.ts` y la navegación lateral en `main-layout.component.html` fueron rediseñados.

| Dominio Legacy (`features/`)      | Estado  | Dominio Nuevo (`features/`)       | Justificación |
| --------------------------------- | ------- | --------------------------------- | ------------- |
| `products/`                       | ❌ Eliminado | `services/`                    | Los "productos" de Neversion son estrictamente Servicios Digitales (Netflix, Spotify). Se limpió la ambigüedad con inventario físico. |
| `users/` (`user-guest.model.ts`)  | ❌ Eliminado | `clients/`                     | Mapeo 1:1 con el concepto de Negocio: Los clientes finales. |
| Gestión de Perfiles Independiente  | ❌ Eliminado | **Anidado en `accounts/`**     | Los perfiles ya no son entidades flotantes. Administrativamente solo existen y se configuran *dentro* de una cuenta. |

---

## 2. Refactorización de Servicios (`features/services`)

Se migró completamente el antiguo módulo de "Productos". Se ha reemplazado todo rastro de iteraciones de 2 o 3 pasos del pasado donde se generaban "Variantes" complejas.

**Cambios y limpieza técnica:**
- **Modelo Adaptado:** `ServiceRequest` y `ServiceResponse` ahora manejan nativamente las propiedades `maxProfiles`.
- **Integración de `details` (JSONB):** Como el backend agrupa propiedades misceláneas en metadata, se adaptaron las vistas y formularios para consumir e inyectar `service.details.category`, `service.details.description` y `service.details.imageUrl`.
- **Formularios Reactivos Netos (`service-form.component`):** Eliminadas las dependencias de selectores obsoletos. Nuevo esquema visual (tags colorizados nativos de Bootstrap 5 y validación explícita para la creación de perfiles máximos).
- **Lista y Tablas Reactivas:** Se empleó angular `Signals` estricto en lugar de RxJS pipes antiguos para renderizar dinámicamente la tabla tras una creación/edición.

---

## 3. Integración Abosoluta de Cuentas y Perfiles (`features/accounts`)

**Antes:** La interfaz te obligaba a manejar Cuentas y Perfiles desconectados, con vistas confusas donde crear un perfil parecía no tener contexto de a qué servicio pertenecía.

**Ahora (La Vista Unificada):**
- **Accordion View (`accounts-list.component`):** La tabla de cuentas fue transformada en un modelo de "Acordeón". Cada cuenta despliega sus slots (Perfiles) automáticamente.
- **`AccountSlotListComponent` Inteligente:** 
  - Ya no es solo un componente visualizador "Dumb". Ahora aloja lógica modal interna.
  - Al presionar "Editar" en un slot dentro de una cuenta, se abre un modal inyectado con Angular ReactiveForms.
  - El modal permite establecer el **Nombre**, el **PIN de acceso** y un **Toggle Switch para establecer al Perfil como "Es Propietario" (`isOwner`)**.
  - Este modelo se comunica a través del nuevo `ProfileService` directamente al backend para parchear la cuenta matriz.

---

## 4. Evolución de Formularios de Suscripciones y Clientes

### Suscripciones (`features/subscriptions`)
- **Problema de Escalabilidad resuelto:** El antiguo `subscription-form.component` utilizaba un `<select>` nativo tradicional que pre-cargaba a TODOS los clientes y cuentas en memoria, causando que crear una suscripción fuera inmanejable con más de 100 clientes.
- **Implementación Typeahead:** 
  - Se modificó totalmente integrando un sistema de búsqueda en vivo (Input). 
  - Con Angular Signals, ahora el backend (o data mock) filtra clientes sobre la marcha ya sea escribiendo el Nombre o el Número de Teléfono en un modal desplegable "dropdown" asíncrono.
  - Esto facilita la asociación veloz Usuario -> Cuenta -> Perfil al generar la facturación recurrente.

### Clientes (`features/clients`)
- Creado explícitamente partiendo de la limpieza del obsoleto módulo de usuarios.
- **Validaciones Actualizadas (`client-form.component`):** Se agregó la estricta validación del campo de Teléfono (`Validators.required`), mostrando de forma condicional el `invalid-feedback` nativo de Bootstrap 5 si no se suministró data.

---

## 5. Control de Calidad Estético y Tecnológico
- El panel ahora es un **Single Page Application 100% Standalone**, sin módulos anidados legados de Angular 14/15.
- Todos los fallos de inyección de estado de Signals (`computed`, `signal`, `@ViewChild`) han sido corregidos y re-testeados (`pnpm run build` completado con éxito).
- Los modales utilizan directamente la API nativa de Javascript de **Bootstrap 5.x** en lugar de depender de empaquetadores externos que rompen con el Server Side Rendering (SSR) o generan leaks de memoria en el DOM.
