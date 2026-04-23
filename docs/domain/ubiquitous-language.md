# B.1 Ubiquitous Language Glossary

## Entidades Core
| Término | Definición |
| :--- | :--- |
| **Servicio** | Producto digital ofrecido en la tienda (ej. Netflix, Disney+). Define precio, duración y cantidad máxima de perfiles por cuenta. |
| **Cuenta** | Credencial base de un servicio (email + contraseña). Es la unidad operativa que el vendedor adquiere y administra. |
| **Perfil** | Unidad vendible dentro de una cuenta. Puede tener PIN. Es el acceso real que se entrega al cliente. |
| **Suscripción** | Relación activa entre un cliente y un perfil o cuenta completa. Tiene vigencia, estado y precio pactado. |

## Actores
| Término | Definición |
| :--- | :--- |
| **Vendedor** | Actor que opera su negocio en el sistema. Gestiona cuentas, aprueba pagos y confirma asignaciones. |
| **Cliente** | Usuario final que compra, sube comprobantes y consulta sus accesos desde su panel. |
| **Super Admin** | Actor con control total. Puede intervenir cualquier negocio, vendedor o suscripción. |

## Transacciones y Procesos
| Término | Definición |
| :--- | :--- |
| **Reservación** | Orden temporal creada durante el checkout con expiración de 1 hora. Actúa como puente antes de generar una orden persistente. Permite reservar disponibilidad sin comprometer el inventario permanentemente. |
| **Orden** | Transacción comercial que origina una compra. Contiene items, comprobante de pago y ciclo de aprobación. |
| **Comprobante** | Evidencia de pago subida por el cliente para validación manual por el vendedor. |
| **Asignación** | Acto de vincular un perfil o cuenta completa a una suscripción, confirmado por el vendedor. |
| **Renovación** | Extensión de una suscripción existente. Sujeta a una regla de gracia de 2 días. |
| **Revocación** | Eliminación del acceso de un perfil cuando una suscripción no se renueva. |
| **Fecha de vencimiento** | Fecha en que expira una suscripción (`due_date` en la base de datos). |
| **Migración inicial** | Carga manual de datos existentes (clientes, cuentas, suscripciones) antes del arranque del sistema. |

## Espacios e Interfaces
| Término | Definición |
| :--- | :--- |
| **Tienda** | Canal de venta del vendedor visible al cliente. Cada vendedor tiene su propia tienda. |
| **Panel del Vendedor** | Interfaz operativa interna donde el vendedor gestiona su negocio. |
| **Panel del Cliente** | Interfaz donde el cliente consulta sus suscripciones, accesos y órdenes. |
