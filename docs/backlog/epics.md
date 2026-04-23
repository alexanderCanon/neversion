# EPICS

Este documento lista los grandes bloques de funcionalidad (Épicas) que componen el sistema Neversion, organizados cronológicamente para la implementación.

| ID | Épica | Descripción |
| :--- | :--- | :--- |
| **EPIC-00** | **Foundation** | Alinear el sistema existente al modelo objetivo definido. Prerrequisito para todas las demás épicas. |
| **EPIC-01** | **Autenticación y Roles** | Registro, login y control de acceso por rol para los tres actores del sistema. |
| **EPIC-02** | **Gestión de Servicios** | CRUD completo de servicios con pricing configurable por vendedor. |
| **EPIC-03** | **Gestión de Cuentas y Perfiles** | CRUD de cuentas y perfiles con estados operativos y disponibilidad. |
| **EPIC-04** | **Gestión de Clientes** | CRUD de clientes vinculados a un vendedor. |
| **EPIC-05** | **Órdenes y Comprobantes** | Flujo completo de creación de orden, subida de comprobante y aprobación de pago. |
| **EPIC-06** | **Asignación y Entrega** | Sugerencia de perfil disponible, confirmación del vendedor y entrega de accesos. |
| **EPIC-07** | **Suscripciones** | Gestión del ciclo de vida: activación, renovación, mora y revocación. |
| **EPIC-08** | **Notificaciones por Correo** | Envío automático de correos para eventos de negocio y recordatorios. |
| **EPIC-09** | **Panel del Cliente** | Interfaz para que el cliente consulte sus suscripciones, accesos e historial. |
| **EPIC-10** | **KPIs del Vendedor** | Dashboard con indicadores clave de rentabilidad y operación en tiempo real. |
| **EPIC-11** | **Migración Inicial** | Carga manual de datos existentes para arranque operativo inmediato. |