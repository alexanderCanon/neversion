# Plan de Implementación: EPIC-04 — Gestión de Clientes (Panel Administrativo)

## 1. Análisis y Diagnóstico
La EPIC-04 requiere la implementación completa del módulo de gestión de clientes para el Vendedor en la aplicación `panel`. Actualmente, el componente `clients-list` y el `client-form` tienen una base funcional, pero no cumplen con los criterios de aceptación completos definidos en `US-029`, `US-030`, `US-031` y `US-032`:
- **US-029 (Listar):** Falta mostrar la cantidad de suscripciones activas (`activeSubscriptionCount`).
- **US-030 (Detalle):** No existe una vista de detalle que muestre las suscripciones activas y el historial de órdenes del cliente.
- **US-031 (Crear):** El formulario actual requiere `phone` y tiene `email` como opcional. Debe ser al revés.
- **US-032 (Editar):** El campo `email` debe ser de solo lectura durante la edición.

## 2. Estrategia Propuesta
1. **Modelos y Servicios (Parcialmente Completado):**
   - Actualizar los modelos en `@neversion/models` para reflejar las nuevas propiedades del API Client (`activeSubscriptionCount`, `ClientDetail`, etc.).
   - Actualizar `ClientsService` para incluir el método `getClientDetail` y mapear correctamente los datos.
2. **Ajuste del Formulario (US-031, US-032):**
   - Modificar `ClientFormComponent` para que `email` sea requerido (`Validators.required`, `Validators.email`).
   - Hacer que `phone` sea opcional.
   - Si el modo es `EDIT`, deshabilitar (o hacer `readonly`) el campo `email`.
3. **Ajuste del Listado (US-029):**
   - Modificar la plantilla `clients-list.component.html` para incluir una columna con el número de suscripciones activas.
   - Añadir un botón o enlace en la tabla para navegar a la vista de detalle.
4. **Vista de Detalle (US-030):**
   - Crear el componente `ClientDetailComponent` (standalone, en `apps/panel/src/app/features/clients/pages/client-detail/`).
   - Consumir el `ClientsService.getClientDetail` e inyectar los datos en la vista mediante Signals o RxJS (`async` pipe / `toSignal`).
   - Diseñar la vista con pestañas (Tabs) o secciones para "Información", "Suscripciones Activas" e "Historial de Órdenes".
5. **Enrutamiento:**
   - Registrar la ruta `clients/:id` en `app.routes.ts` protegiéndola con `roleGuard`.
6. **Bitácora:**
   - Registrar la finalización de las historias de usuario en `docs/implementation/panel-construction.md`.

## 3. Plan de Implementación (Secuencial)

1. **Paso 1: Ajuste de Formulario y Listado**
   - Modificar `client-form.component.ts` y `.html`.
   - Modificar `clients-list.component.html`.
2. **Paso 2: Vista de Detalle**
   - Generar/crear archivos para `ClientDetailComponent`.
   - Implementar lógica y maquetación de detalle.
3. **Paso 3: Integración de Rutas**
   - Actualizar `app.routes.ts`.
4. **Paso 4: Verificación y Bitácora**
   - Registrar en `/docs/implementation/panel-construction.md`.

## 4. Validación
Una vez implementados los pasos anteriores, solicitaré al humano que ejecute:
```bash
cd apps/panel
pnpm run lint
pnpm run build
```
Si todo compila correctamente y no hay errores de lint, se dará por concluido el módulo.
