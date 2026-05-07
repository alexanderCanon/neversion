# Modelo de Dominio

Este documento describe el modelo de dominio del sistema **Neversion**, basado en el análisis de descubrimiento y el esquema de datos actual.

## Dominios del Sistema

### 1. Core Domain (Gestión de Suscripciones e Inventario)
Es el corazón del negocio. Se encarga de la administración de los recursos técnicos (cuentas y perfiles) y su asignación a los clientes finales.

*   **Servicio (Service)**: Define el catálogo de productos digitales (ej: Netflix, HBO). Establece las reglas de precio, duración y capacidad (perfiles).
*   **Cuenta (Account)**: Representa la credencial maestra adquirida por el vendedor. Contiene la información técnica de acceso.
*   **Perfil (Profile)**: Sub-división de una cuenta que representa un "slot" individual de acceso. Es la unidad mínima de venta.
*   **Suscripción (Subscription)**: La relación comercial activa. Vincula a un cliente con un perfil o cuenta por un periodo determinado.

### 2. Supporting Domain (Ventas y Operaciones)
Soporta el flujo comercial necesario para que el dominio core se active.

*   **Reservación (Reservation)**: Bloqueo temporal de inventario mientras el cliente completa el pago y sube su comprobante.
*   **Orden (Order)**: Registro formal de una transacción aprobada. Es el disparador para la creación de suscripciones.
*   **Vendedor (Vendor)**: Raíz de agregación para el modelo multi-inquilino. Define el aislamiento de datos y la marca propia de cada negocio.
*   **Cliente (Client)**: Usuario final registrado bajo un vendedor específico.

### 3. Generic Domain (Identidad y Notificaciones)
Funcionalidades comunes que no son específicas del negocio de streaming pero son necesarias.

*   **Usuario (User)**: Entidad de identidad y autenticación. Maneja los roles (Super Admin, Vendedor, Cliente).
*   **Registro de Notificaciones (Notification Log)**: Registro de auditoría de las comunicaciones enviadas por correo.

## Relaciones Clave

*   **Aislamiento**: Un **Vendor** es dueño de sus propios **Clients**, **Services**, **Accounts**, **Orders** y **Subscriptions**.
*   **Jerarquía de Inventario**: Un **Service** se provee a través de múltiples **Accounts**, y cada **Account** contiene múltiples **Profiles**.
*   **Flujo Comercial**: Una **Reservation** incluye múltiples detalles de servicios y puede convertirse en una **Order** tras la validación.
*   **Cumplimiento**: Una **Order** genera la creación de **Subscriptions** y activa los **Profiles** correspondientes.
