# Discovery — MVP Sistema de Reventa de Cuentas de Streaming

## 1. Resumen ejecutivo
Este documento consolida la **Fase A — Discovery** del proyecto **Neversion**

- **Backend**
- **Frontend de administración/operación & tienda/cliente**

El propósito del MVP es resolver los principales dolores operativos del negocio actual, que hoy depende fuertemente de **WhatsApp**, seguimiento manual y registros en **Excel**.

El sistema debe cubrir tanto la **operación interna del vendedor** como la **experiencia del cliente final**, permitiendo vender, registrar, asignar, renovar y controlar cuentas/suscripciones desde una sola plataforma.

---

## 2. Problema actual

Actualmente la operación del negocio presenta las siguientes limitaciones:

- La captación y venta ocurre principalmente por **WhatsApp**.
- La validación de pagos es **manual**.
- La entrega de accesos se realiza manualmente.
- El registro de ventas y suscripciones se lleva en **Excel**.
- El seguimiento de renovaciones requiere revisión diaria manual.
- Existen riesgos de olvidar renovaciones próximas.
- La operación no cuenta con un canal digital propio con mayor visibilidad comercial.

Esto provoca ineficiencia operativa, dependencia de procesos manuales y pérdida potencial de renovaciones o seguimiento oportuno a clientes.

---

## 3. Objetivo del MVP

El MVP debe servir principalmente para:

- **Centralizar el control de cuentas y expiraciones**.
- **Reducir la operación manual actual**.
- **Automatizar notificaciones por correo** cuando se acerquen fechas de renovación.
- **Aumentar visibilidad comercial**, reduciendo la dependencia exclusiva de WhatsApp.
- **Apoyar la venta y la operación diaria** desde un sistema propio.

---

## 4. Visión de producto del MVP

El producto no será solo una tienda. Será una combinación de:

- **herramienta operativa interna**, para control de cuentas, clientes, suscripciones y renovaciones;
- **canal de venta**, para que el cliente pueda registrarse, comprar, subir comprobantes, consultar sus accesos y revisar sus suscripciones.

El sistema nace para el negocio actual del propietario, pero debe quedar con una base suficientemente ordenada como para que en el futuro pueda evolucionar hacia un producto reutilizable para terceros.

---

## 5. Alcance del MVP

### 5.1 Incluye

- Gestión de **servicios**.
- Gestión de **clientes**.
- Gestión de **cuentas** (credenciales base del servicio).
- Gestión de **perfiles** generados por cuenta.
- Gestión de **suscripciones**.
- Gestión de **órdenes**.
- Flujo de **registro/login** del cliente.
- Flujo de **compra con comprobante**.
- Flujo de **validación manual de pago**.
- Flujo de **asignación semiautomática** de cuenta/perfil.
- Envío de **correos automáticos** para eventos definidos.
- Panel del **vendedor**.
- Panel del **cliente**.
- Panel del **super admin**.
- Soporte para **carga/migración manual inicial** de datos activos existentes.

### 5.2 Fuera del MVP

Estas capacidades quedan explícitamente **fuera del alcance de la primera versión**, pero deben documentarse como evolución futura:

- Pasarela de pago automática.
- Validación automática real de comprobantes por IA.
- Integración con WhatsApp.
- Multimoneda real.
- Multipaís.
- Multivendedor real en operación completa.
- Reportes avanzados.
- Analítica detallada.
- Asignación totalmente automática de cuentas.
- Aplicación móvil.

---

## 6. Actores del sistema

### 6.1 Super Admin
Responsable del control total del sistema.

Debe poder:

- crear y administrar vendedores;
- ver todos los negocios;
- activar o desactivar tiendas;
- ver métricas globales;
- definir configuraciones base del sistema;
- gestionar planes/licencias a futuro si el sistema se comercializa;
- intervenir cuentas, órdenes o suscripciones de cualquier vendedor.

### 6.2 Vendedor
Responsable de operar su propio negocio dentro del sistema.

Debe poder:

- gestionar servicios;
- gestionar cuentas y perfiles;
- ver disponibilidad;
- aprobar pagos;
- confirmar asignaciones sugeridas;
- completar órdenes y enviar accesos;
- ver suscripciones por vencer;
- renovar suscripciones;
- revocar accesos;
- ver clientes;
- consultar KPIs básicos;
- crear clientes manualmente;
- crear suscripciones manualmente.

### 6.3 Cliente
Usuario final que compra y administra sus servicios adquiridos.

Debe poder:

- registrarse e iniciar sesión;
- crear órdenes;
- consultar disponibilidad desde la tienda;
- subir comprobante de pago;
- ver sus suscripciones activas;
- ver fechas de vencimiento;
- ver historial de órdenes;
- ver estado de pagos;
- ver accesos asignados;
- renovar suscripciones;
- comprar nuevos servicios;
- editar sus datos básicos;
- recibir notificaciones por correo.

---

## 7. Modelo operativo actual (AS-IS)

El flujo real actual del negocio es:

1. El cliente pregunta por disponibilidad vía **WhatsApp**.
2. Se le ofrecen precios por **perfil individual** o **cuenta completa**, junto con los métodos de pago disponibles.
3. Si se recibe el pago, este se valida manualmente.
4. Se preparan y envían los accesos en formato similar a:
   - Cuenta
   - Correo
   - Contraseña
   - Perfil y PIN (si aplica)
   - Fecha de renovación
5. La venta/suscripción se registra manualmente en **Excel**.
6. Comienzan a correr los días adquiridos.
7. Manualmente se revisan próximas renovaciones.
8. Si el cliente desea renovar, vuelve a pagar por depósito o transferencia.
9. Si no renueva, se le revoca el acceso.

### Hallazgos clave del flujo actual

- Existe alta dependencia de memoria y disciplina operativa.
- La revisión de expiraciones no es escalable.
- No existe automatización formal de recordatorios.
- El sistema actual no separa claramente operación, venta, asignación e historial.

---

## 8. Flujo objetivo de alto nivel (TO-BE)

1. El cliente entra a la **tienda** del vendedor.
2. Consulta servicios, precios y modalidades disponibles.
3. Se registra con:
   - nombre
   - teléfono
   - correo
   - contraseña
4. Crea una orden.
5. Realiza el pago por depósito o transferencia.
6. Sube el comprobante.
7. El vendedor, un tercero autorizado o un bot en el futuro valida el comprobante.
8. La orden pasa a **Aprobado**.
9. El sistema sugiere una cuenta/perfil disponible.
10. El vendedor confirma la asignación.
11. Se entregan accesos.
12. La orden pasa a **Completada**.
13. Se crea/actualiza la suscripción del cliente.
14. El cliente recibe correo y puede consultar todo desde su panel.
15. Antes del vencimiento, el sistema envía recordatorios por correo.

---

## 9. Módulos principales del MVP

### 9.1 Servicios
Representan los servicios digitales ofrecidos, inicialmente con categoría streaming.

Datos mínimos:

- nombre del servicio;
- categoría;
- descripción;
- imagen;
- precio por perfil;
- precio por cuenta completa;
- cantidad máxima de perfiles por cuenta;
- duración;
- activo / inactivo.

### 9.2 Clientes
Representan a los compradores de la tienda de un vendedor específico.

Datos mínimos confirmados:

- nombre;
- teléfono;
- correo;
- contraseña.

### 9.3 Cuentas
Representan la credencial base operativa de un servicio.

Una cuenta debe tener:

- servicio al que pertenece;
- correo;
- contraseña;
- tipo de venta: completa o dividida;
- cantidad de perfiles generados;
- costo de adquisición;
- fecha de compra de la cuenta matriz;
- fecha de expiración de la cuenta matriz;
- estado;
- observaciones;
- distribuidor / fuente de adquisición.

### 9.4 Suscripciones
Representan la relación activa entre cliente y servicio vendido.

Una suscripción debe tener:

- cliente;
- servicio;
- cuenta asignada;
- perfil asignado, si aplica;
- tipo de venta: perfil o cuenta completa;
- fecha de inicio;
- fecha de vencimiento;
- estado;
- precio vendido;
- descuento aplicado;
- orden origen;
- notas.

### 9.5 Órdenes
Representan la transacción comercial que origina la compra.

Una orden debe tener:

- cliente;
- vendedor;
- items de la orden;
- subtotal;
- descuento total;
- total final;
- moneda;
- estado;
- método de pago;
- comprobante de pago;
- fecha de creación;
- fecha de aprobación;
- observaciones.

### 9.6 Notificaciones
Módulo responsable del envío de correos para eventos relevantes del negocio.

---

## 10. Reglas de negocio clave

### 10.1 Venta por perfil o cuenta completa
El sistema debe soportar ambos escenarios:

- venta por **perfil individual**;
- venta de **cuenta completa**.

### 10.2 Estructura de cuenta
- Una cuenta pertenece a **un solo servicio**.
- Una cuenta puede tener **varios perfiles**.
- La cantidad de perfiles vendibles por cuenta es definida por el vendedor.
- Cada perfil puede tener **PIN**.

### 10.3 Cuenta dividida vs cuenta completa
- Una cuenta puede venderse de manera dividida o completa.
- En la venta completa, la cuenta queda asociada operativamente a un solo cliente.
- Aun así, se mantiene el control de la cuenta matriz y su ciclo de vida.

### 10.4 Perfiles como slots reales
- Los perfiles deben **generarse automáticamente**.
- Cada perfil pasa por estados operativos.
- El sistema debe manejar perfiles reales y no solo contadores.

### 10.5 Estados de perfil / disponibilidad
Se requiere soportar estados operativos como:

- disponible;
- reservado;
- vendido / activo;
- vencido;
- bloqueado.

### 10.6 Vigencia y duración
- La duración estándar será **30 días**.
- El sistema debe soportar **otras duraciones** definidas por el vendedor.

### 10.7 Regla de renovación tardía
- Si el cliente paga **hasta 2 días tarde**, se considera renovación bajo la lógica anterior.
- Si paga **3 días o más tarde**, se considera una nueva alta / nueva fecha desde el día de pago.

### 10.8 Inicio de vigencia
La vigencia normalmente comienza **desde el día de pago**, salvo que aplique la regla de gracia definida.

### 10.9 Revocación de acceso
Cuando no se renueva, la operación real estándar será:

- **eliminar el perfil**.

El cambio de contraseña de la cuenta completa es poco frecuente y no será el mecanismo principal.

### 10.10 Suscripciones múltiples por cliente
El sistema debe soportar que un cliente tenga:

- varios servicios simultáneamente;
- combos de varios servicios;
- más de una suscripción del mismo servicio, si el negocio lo requiere;
- compras individuales y cuentas completas.

### 10.11 Relación vendedor-cliente
- Cada cliente pertenece a **un solo vendedor**.
- Cada vendedor opera su **tienda específica y marca propia**.
- Los vendedores no necesitan estar relacionados entre sí.

### 10.12 Alcance geográfico y monetario
- El MVP opera únicamente en **Guatemala**.
- La moneda base es **GTQ (quetzal)**.
- A futuro el sistema no debería quedar cerrado conceptualmente a otros países/monedas.

### 10.13 Costos y rentabilidad
- La cuenta matriz tiene un **costo de adquisición**.
- La venta de perfil/cuenta tiene un **precio de venta**.
- El sistema debe permitir medir al menos de forma básica la rentabilidad general del período.

### 10.14 Pricing configurable
El precio puede variar por:

- servicio;
- vendedor;
- duración.

No debe quedar fijo en código.

### 10.15 Descuentos por combo
- El descuento se calcula automáticamente en carrito.
- Inicia a partir de cierto número de servicios agregados.
- Debe soportar descuentos incrementales por cantidad.
- Los porcentajes deben ser configurables.

---

## 11. Ciclo de vida de órdenes

Estados confirmados:

- **Pendiente de pago**
- **Comprobante enviado**
- **Aprobado**
- **Completada**

### Significado de estados críticos

**Aprobado**
- El comprobante es válido.
- Corresponde a fecha y hora válida.
- Corresponde a la cuenta bancaria correcta.
- Puede ser aprobado por el vendedor, un tercero o un bot a futuro.
- Aún queda pendiente la entrega de accesos.

**Completada**
- Los accesos ya fueron entregados.
- La suscripción quedó creada/activada.

---

## 12. Asignación de cuentas y entrega de accesos

La entrega de accesos en el MVP será **semiautomática**.

### Flujo esperado
- El sistema sugiere una cuenta/perfil disponible.
- El vendedor confirma la sugerencia.
- El cliente recibe accesos por correo.
- El cliente también puede verlos en su panel.

---

## 13. Alcance funcional del panel del vendedor

El vendedor debe poder:

- crear, editar y administrar servicios;
- crear, editar y administrar cuentas;
- visualizar disponibilidad de cuentas/perfiles;
- aprobar pagos manualmente;
- confirmar asignaciones sugeridas;
- completar órdenes y enviar accesos;
- ver suscripciones por vencer;
- renovar suscripciones;
- revocar acceso;
- ver clientes;
- crear clientes manualmente;
- crear suscripciones manualmente;
- revisar indicadores básicos del negocio.

### Nota operativa relevante
El sistema debe soportar tanto:

- ventas originadas desde la **tienda**, como
- operaciones cargadas manualmente desde el **panel**, debido a la migración y continuidad del proceso actual.

---

## 14. Alcance funcional del panel del cliente

El cliente debe poder:

- registrarse;
- iniciar sesión;
- ver sus suscripciones activas;
- ver fecha de vencimiento;
- ver historial de órdenes;
- ver estado de pagos;
- subir comprobante;
- ver accesos asignados;
- renovar una suscripción;
- comprar nuevos servicios;
- editar sus datos básicos.

---

## 15. Notificaciones por correo del MVP

Eventos confirmados:

- registro exitoso;
- orden creada;
- comprobante recibido;
- pago aprobado;
- accesos enviados;
- recordatorio de renovación;
- suscripción vencida;
- renovación confirmada.

---

## 16. Migración inicial requerida

Para empezar a operar sin arrancar en blanco, la migración inicial debe contemplar:

- clientes existentes;
- servicios existentes;
- cuentas existentes;
- suscripciones activas;
- fechas de vencimiento.

### Fuera de la migración inicial
Por ahora no se prioriza migrar histórico completo de órdenes antiguas, comprobantes históricos, reportes históricos complejos. Documentado como evolución futura si llega a ser necesario.

---

## 17. KPIs / reportes mínimos del MVP

El sistema debe ofrecer como mínimo:

- suscripciones por vencer **hoy**;
- suscripciones por vencer **mañana**;
- suscripciones por vencer **esta semana**;
- cuentas disponibles vs ocupadas;
- renovaciones logradas;
- clientes activos;
- ganancia total del período.

### No prioritarios para el MVP
Se documentan, pero no son prioritarios en esta fase:

- órdenes pendientes de aprobación como KPI formal;
- ventas del mes como reporte consolidado avanzado;
- ganancia/pérdida por cuenta individual.

---

## 18. Restricciones operativas del MVP

Quedan confirmadas las siguientes restricciones:

- solo **Guatemala**;
- moneda principal **GTQ**;
- pagos solo por **depósito** o **transferencia**;
- validación de pago **manual**;
- notificaciones por **correo**;
- sin integración con **WhatsApp**;
- sin **pasarela de pago**.

---

## 19. Criterios de éxito del MVP

Se considerará exitoso el MVP si logra:

- dejar de depender de **Excel**;
- reducir olvidos de renovación;
- lograr uso real del **panel** por parte de clientes;
- enviar recordatorios automáticos por correo sin fallos relevantes;
- registrar las suscripciones dentro del sistema;
- reducir tiempo operativo por venta;
- mejorar la tasa/flujo de renovaciones;
- apoyar mejor la operación comercial general.

---

## 20. Supuestos y riesgos

### 20.1 Supuestos confirmados
- El vendedor seguirá validando pagos manualmente.
- El flujo principal seguirá siendo por **depósito**.

### 20.2 Riesgos confirmados
- Clientes que no revisen el correo.
- Datos actuales incompletos o inconsistentes.
- Resistencia al cambio desde el flujo tradicional por WhatsApp.

---

## 21. Decisiones de producto relevantes tomadas en Discovery

1. El sistema se construirá primero para el negocio actual, pero sin cerrar la puerta a escalarlo más adelante.
2. El MVP no debe sobrecargarse con automatizaciones complejas desde el inicio.
3. El sistema debe cubrir tanto el canal de venta como la operación interna.
4. El cliente tendrá panel propio y autenticación tradicional con correo + contraseña.
5. La asignación de accesos no será totalmente automática en la primera versión.
6. El foco inicial está en resolver el control operativo y las renovaciones.

---

## 22. Próximas fases documentadas (no desarrolladas en este documento)

Las siguientes fases quedan solamente registradas como próximas a construir:

### Phase B — Domain + Analysis
- ubiquitous language glossary;
- domain modeling esencial;
- actor/role map;
- use case catalog;
- business rules document;
- functional requirements.

### Phase C — Architecture Essentials
- decisiones arquitectónicas clave;
- contratos de alto nivel;
- ER diagram;
- non-functional requirements;
- deployment vision.

### Phase D — Backlog & Planning
- product backlog;
- user stories;
- priorización;
- roadmap de sprints;
- Definition of Done.

### Phase E — Implementation
- construcción del backend;
- construcción del panel de administración;
- construcción de la tienda/panel cliente;
- integración progresiva de módulos.

---

Este discovery define el marco funcional y operativo del MVP, así como el alcance real de la primera versión. A partir de aquí, la siguiente etapa recomendada es transformar este contenido en insumos estructurados de modelado de dominio, análisis y backlog.
