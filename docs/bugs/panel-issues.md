# Observaciones y Errores - Panel Administrativo

Este documento registra los problemas y mejoras identificados por Alex para el panel de Neversion.

## 1. Interfaz de Usuario (UI/UX)

- **Contraseña Visible**: Al agregar una cuenta nueva, la contraseña debe ser visible (actualmente aparece oculta con asteriscos).
- **Simplificación de Nombres de Servicio**: En el selector de servicios, mostrar únicamente el nombre (ej. "Netflix") en lugar de incluir metadatos como el número de perfiles (ej. "Netflix (5 perfiles)").
- **Formato de Fechas**: Estandarizar todas las fechas al formato latinoamericano `dd-MM-yyyy`.
    - Afecta a: Fecha de renovación/vencimiento.
    - Afecta a: Fecha de compra.

## 2. Errores de Funcionamiento (Bugs)

- **Cuentas Invisibles**: Se reporta que tras registrar una cuenta, esta no aparece en la lista del panel (muestra "no se encontraron cuentas con los filtros aplicados"), a pesar de que los datos existen correctamente en la base de datos.

## 3. Rendimiento y Red (Performance)

- **Peticiones Duplicadas**: Al cargar o actualizar la página, se detectan peticiones redundantes:
    - 1 petición a `/api/v1/auth/me`.
    - 2 peticiones idénticas a `/api/v1/accounts/vendor/{uuid}`.
- **Seguridad/Headers**: Se confirma que el JWT se está enviando correctamente en el header `Authorization: Bearer` para todas las peticiones internas del vendedor.

---
*Última actualización: 5 de Mayo, 2026*
