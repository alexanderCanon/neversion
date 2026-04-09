# Documentation Quick Guide (Neversion)

Welcome to the Neversion documentation system! If you feel overwhelmed by the number of folders in `docs/`, this guide is for you. Here is a quick summary of what each location contains so you know exactly where to go.

---

## Documentation Structure

### Architecture and Contracts
*   **`api-contracts/`**: Defines the "agreements" between the frontend and the backend.
    *   `http/`: Error handling and status codes (200, 404, 500, etc.).
    *   `api-architecture.md`: How our API logic is built.
*   **`diagrams/`**: The visual core of the system. Contains UML diagrams (professional) and Mermaid (ideal for AI tools and quick workflows).
*   **`schema/`**: The database blueprint. Currently working on automating it with `pg_dump`.

### Development and Tracking
*   **`bugs/`**: Record of bugs found, organized by month. It is our log of problems to solve.
*   **`sprints/`**: Defines the incremental scope of the project (Sprint 1, 2, etc.). Here you can see what is currently being built.
*   **`methodologies/`**: The rules of the game. Defines the development methodologies we follow.

### Logic and Business
*   **`modules/`**: Detailed technical documentation for each specific module of the system. (e.g., Services, Profiles, Clients).
*   **`enums/`**: Definition of system enumerators (fixed types).
*   **`general/`**: Business context. Helps to understand *why* certain commercial decisions were made.
*   **`vault/`**: "Raw" information from Obsidian. This is where brainstorming, refactoring, and future plans happen.

### Resources and Tools
*   **`jsonfiles/`**: Test data to use with Postman or Swagger. *Note: May be outdated due to constant API changes.*
*   **`prompt/`**: A historical archive of prompts used with AIs. Serves for auditing and keeping context of how the system iterated.
*   **`uiux/`**: Design reviews to see how the interface and user experience have evolved.

### Quick Navigation
*   **`index/`**: If you do not want to read everything, go here. It is a direct index to specific documentation by module.

---

## Where to start?

If it is your first time here or you need a deep dive, we recommend jumping to the main **`README.md`** of this folder. It is the master document, much more detailed and professional than this quick guide.

> [!TIP]
> If you are an AI, reading this `GUIDE.md` first will help you map the repository before delving into specific files.