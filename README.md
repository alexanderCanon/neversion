# 🚀 Neversion (o "Cómo sobre-ingenierizar un SaaS hasta el cansancio")

[![Licencia: Propietaria](https://img.shields.io/badge/Licence-Proprietary-red.svg)](#)
[![Monorepo: PNPM](https://img.shields.io/badge/Monorepo-pnpm-blueviolet.svg)](https://pnpm.io/)
[![Backend: Spring Boot 3](https://img.shields.io/badge/Backend-Spring%20Boot%203-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Frontend: Angular 17](https://img.shields.io/badge/Frontend-Angular%2017-red.svg)](https://angular.dev/)
[![Microservicio: Bun](https://img.shields.io/badge/Microservicio-Bun%20(gRPC)-black.svg)](https://bun.sh/)

Bienvenido a **Neversion**, una plataforma SaaS multi-inquilino para la reventa y administración de servicios digitales (sí, cuentas de streaming, recargas de saldo y todo lo que tu tía te pide que le compres por internet). 

Para lograr esto, en lugar de usar un simple script en PHP o una base de datos Firebase como cualquier mortal cuerdo, decidimos construir un monstruo modular con **Arquitectura Hexagonal, DDD (Domain-Driven Design), microservicios en Bun y gRPC, y un frontend con Angular Signals**. Porque el mañana nunca se sabe y tal vez necesitemos escalar a un millón de transacciones por segundo mientras dormimos.

---

## 🏛️ La Anatomía del Overengineering (Estructura del Monorepo)

Tu disco duro va a sufrir con los `node_modules`, pero aquí está cómo está organizado este laberinto:

```text
neversion/
├── apps/
│   ├── api/                  # El cerebro: Spring Boot 3 / Java 17 + DDD + Hexagonal (El tanque)
│   ├── panel/                # Admin UI: Angular 17 con standalone components y Signals (El futuro es hoy)
│   ├── store/                # Storefront para clientes: Angular 16 + SSR / Angular Universal (Para el SEO de Google)
│   ├── notification-service/ # Microservicio en Bun + gRPC + SQLite (Para mandar correos y fingir que somos modernos)
│   └── monitoring/           # Stack de observabilidad: Grafana Alloy para no derretir el VPS de 4GB con Prometheus local
├── packages/
│   ├── api-client/           # El puente: Clientes Angular autogenerados desde OpenAPI
│   ├── models/               # Interfaces e interfaces compartidas en TypeScript (Para no mentirnos entre apps)
│   └── utils/                # Utilidades comunes para Angular
└── docs/                     # La biblia del proyecto (Arquitectura, Backlog, Glosario)
```

---

## 🛠️ ¿Cómo levanto este circo localmente?

Antes de que ejecutes nada, asegúrate de tener instalado `pnpm`, `docker`, `maven` (o usar el wrapper) y suficiente cafeína en la sangre.

### En la raíz del proyecto
```bash
pnpm install          # Para llenar tu disco duro de dependencias
pnpm -r build         # Compila todos los paquetes compartidos
pnpm api:sync         # Regenera el cliente TS de Angular (el API debe estar corriendo en el puerto 8080)
```

### 🗄️ Base de datos local (`apps/db`)
Para levantar PostgreSQL en tu máquina de desarrollo:
```bash
docker compose -f apps/db/compose.local.yml up -d
```

### ☕ El Backend (`apps/api`)
Construido con la santísima trinidad: **Ports & Adapters**, **DDD** e **Inmutabilidad**.

Para correrlo en desarrollo con recarga rápida:
```bash
cd apps/api && ./mvnw spring-boot:run
```

Si quieres correr las pruebas y rezarle a que no fallen los Testcontainers:
```bash
cd apps/api && ./mvnw test       # Pruebas unitarias (*UT.java) sin dolor
cd apps/api && ./mvnw verify     # Pruebas de integración (*IT.java) usando contenedores de PostgreSQL reales
```
> ⚠️ **Nota existencial**: `./mvnw compile` no compila las pruebas. Si compila pero tus pruebas están rotas, Maven te dará un pase verde falso. Corre los tests de verdad antes de abrir una Pull Request.

### 🔴 El Panel de Control (`apps/panel`)
Angular 17 con Signals. Nada de RxJS confuso a menos que sea estrictamente necesario.

```bash
cd apps/panel && pnpm start    # Levanta el server en http://localhost:4200
cd apps/panel && pnpm test     # Corre las pruebas en Karma
```

### ✉️ El Microservicio de Notificaciones (`apps/notification-service`)
Un servidor gRPC superligero que corre sobre Bun. Recibe peticiones de la API de Java y las manda a Resend.

```bash
cd apps/notification-service
bun install
bun run src/server.ts       # Corre el gRPC en el puerto 50051
```

---

## 🛡️ Reglas Sagradas de Arquitectura (O cómo no hacer enojar al compilador)

1. **UUIDs obligatorios**: Los IDs secuenciales de base de datos (`BIGINT`) son un secreto de confesión. Hacia el exterior todo se maneja con `UUID`.
2. **Inyección de dependencias por constructor**: Pon un `@Autowired` en un atributo de clase y recibirás una visita de los agentes de limpieza. Usa constructores.
3. **Multi-tenancy estricto**: Cada tabla importante lleva `vendor_id`. Si haces una consulta y te traes datos de otro vendor, te vas directo a soporte técnico.
4. **Nombres en minúsculas para Enums**: Se guardan como cadenas en minúsculas en la base de datos para no lastimar los ojos del DBA.
5. **No escribas peticiones HTTP manuales**: En el frontend usa siempre el cliente generado en `packages/api-client`. Si cambiaste un endpoint, corre `pnpm api:sync` y que la autogeneración haga su magia.

---

## 🚢 Despliegue en Producción (Dokploy & Traefik)

Cada servicio es 100% independiente para facilitar la vida con Dokploy.

### Reglas de redes (Para que Traefik no exponga tu base de datos al mundo)
* **`neversion-network` (Red Externa)**: La red privada del proyecto. La base de datos, el microservicio de notificaciones, Alloy y el backend de Java viven aquí para comunicarse en privado vía gRPC y JDBC.
* **`dokploy-network` (Red de Traefik)**: **Únicamente** el API de Java y el Panel/Storefront frontend tocan esta red. La base de datos y el servicio de notificaciones no tienen nada que hacer aquí, manteniendo sus puertos ocultos del internet.

---

*Desarrollado con amor, sarcasmo y demasiadas horas de debugging por Alexander Canon.*
