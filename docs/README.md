# Neversion System Documentation

Welcome to the central repository for the Neversion project documentation. All source of truth resides within this `docs/` directory.

> [!TIP]
> **Feeling overwhelmed by the folder structure?** Check out the [Quick Guide (GUIDE.md)](GUIDE.md) for a friendly map of all subdirectories and their purpose.

The project operates in **3 fundamental phases**. The documentation directory is strictly organized to reflect this composition. Iterate between these phases continually until the project is completed.

---

## Phase 1: Analysis
This phase defines the "What" and the "Why". It establishes the business vision, user stories, use cases, scope, and the methodologies used to execute the project.

**Where to read:**
* **`business-context.md`** / **`business-rules.md`** / **`domain.md`**: Start here. These three root indexes map out the business glossary, cross-cutting rules, and overarching concepts.
* **`modules/`**: The detailed breakdown of the system. Contains all Use Cases, validation structures, and specific business rules separated logically (e.g., `services`, `profiles`, `clients`).
* **`sprints/`**: Defines the functional requirements, scopes, and objectives separated by MVP targets (e.g., `sprint1-neversion-system.md`).
* **`software-development-methods.md`**: Defines the operative frameworks: **Incremental Development (Sprints)** and **Scrum**.
* **`general/`** & **`march/`**: Historical and overarching project context logs.

---

## Phase 2: Architecture
Real work begins after a deep initial analysis. This phase defines the "How" at a high level. It covers system components, structural integrity, and infrastructure decisions.

**Where to read:**
* **`system-architecture.md`**: The structural backbone outlining system architecture, tech stack boundaries, and infrastructure/cloud hosting decisions.
* **`schema/database-schema.md`**: Bridges the gap between Domain Rules and the literal relational PostgreSQL database (Flyway migrations).
* **`enums/system-enums.md`**: The strictly defined system-wide dictionary and state machines (System Enums).
* **`diagrams/`** & **`draw/`**: Visual models of the architecture.

---

## Phase 3: Engineering
This defines everything related to logic, programming, and software development frameworks. It is the final execution step after performing analysis and defining the architecture. Applies design patterns (SOLID, Factory, Builder, KISS).

**Where to read:**
* **`api-contracts/`**: The absolute source of truth for backend development. Contains the exact REST endpoints, JSON structures, DTOs, and global `api-architecture` mapping to Phase 1 requirements.
* **`bugs/`**: Ongoing engineering logs detailing tracked issues, replication steps, and technical debt.