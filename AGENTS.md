# Project Agent Coordinator — Single Source of Truth

This document defines the roles, responsibilities, and technical constraints for all AI agents participating in the development of the Neversion platform. It serves as the global governance framework to maintain system integrity across multiple specialized agents.

> [!IMPORTANT]
> **Zero Guesswork Policy**: Agents are prohibited from making product, business, or architectural decisions. If a requirement is undocumented or ambiguous, you must stop and report immediately using the Escalation Protocol.

---

## 🤖 Agent Roles & Assignments

| Role | Primary Scope | Operational Protocol | Target Technology |
| :--- | :--- | :--- | :--- |
| **Backend Architect** | \`/apps/api\` | Hexagonal Architecture & DDD | Spring Boot 3 (Java 17) |
| **Admin UI Expert** | \`/apps/panel\` | Angular 17+ & Signals | Angular (Standalone) |
| **Client UI Expert** | \`/apps/store\` | Angular 16 & RxJS | Angular (Modules) |

---

## 💎 Global Engineering Mandates (All Agents)

### 1. Absolute Type Safety
- **No \`any\` allowed.** Under any circumstance.
- Use \`unknown\` with Type Guards or explicit interfaces from \`@neversion/models\`.
- All frontend mapping must align with the generated \`@neversion/api-client\`.

### 2. Surgical Precision & Planning
- Follow the **Analysis -> Proposal -> Plan -> Act -> Validate** cycle.
- Do not refactor unrelated code. Focus strictly on the assigned task.
- Before editing source code, a plan must be approved by the human operator.

### 3. Source of Truth & Logging
- The \`/docs\` directory is the immutable source of truth for business rules and ubiquitous language.
- Agents **must** register every change, decision, or progress in the corresponding bitácora in \`/docs/implementation/\`.

### 4. Module Gate — Non-Negotiable Validation
- Each module or user story must be validated with tests **before** moving to the next.
- **Stop** and request the user to run tests after implementation.
- Do not proceed until the user confirms tests are green.

---

## 🛠 Command Constraints

### Allowed Commands
\`\`\`bash
# Dependency Management
pnpm install
mvn dependency:resolve

# Validation & Testing
mvn test                        # Unit tests (Backend)
mvn verify                      # Integration tests (Backend)
pnpm run test                   # Unit tests (Frontend)
pnpm run test -- --watch=false  # CI execution (Frontend)

# Version Control
git add | git commit | git push | git status
\`\`\`

### Forbidden Commands
> [!CAUTION]
> Never execute the following. If a task seems to require them, stop and report.

- **Runtime**: \`mvn spring-boot:run\`, \`pnpm start\`, \`ng serve\`, \`docker-compose up\`.
- **System**: \`pnpm install -g\`, \`pip install\`, \`apt install\`.
- **DB Raw**: \`psql\`, \`liquibase\`, \`flyway migrate\` (unless explicitly authorized for backend).
- **Destructive**: \`rm -rf\`, \`DROP TABLE\`, \`git push --force\`, \`git reset --hard\`.

---

## 🚨 Escalation Protocol (Blockers)

If you encounter missing information, conflicting documentation, or a technical wall, output the following exactly:

\`\`\`text
BLOCKER: [Module Name] — [Scope: API | Panel | Store]
Reason: [Clear description of the gap or forbidden requirement]
Reference: [Target documentation file that requires update]
Action Required: Human intervention needed for resolution.
\`\`\`

---
*Last Updated: April 2026*
