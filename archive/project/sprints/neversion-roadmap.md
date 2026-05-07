# Neversion - Automation

## Context
Streaming services access business (Netflix, Disney+, Spotify, etc.).
The main pain point: **not missing renewal dates** of accounts and client payments.

---

## Defined Stack

| Layer | Technology | Role |
|---|---|---|
| Database | Supabase (PostgreSQL) | Single source of truth |
| Automations | n8n (Dokploy) | Notifications, jobs, asynchronous events |

---

## Phases

### Phase 0 -- Completed
- Notification workflow created in n8n (ID: `uzRMsMJAngKHaBL1`)
- Base logic: milestones 7d / 3d / 1d / due / overdue with audit log
- Validated and scalable architecture

---

### Phase 1 -- Supabase + Fix Workflow (NOW)

#### Step 1: Create project in Supabase
1. Go to [supabase.com](https://supabase.com) -> New project
2. Save: `Project URL`, `anon key`, `service_role key`, `DB password`
3. Go to **SQL Editor** and execute the schema below

#### Step 2: Execute SQL schema
See the **Schema** section below.

#### Step 3: Load initial data from the sheet
Manually migrate (or via CSV import in Supabase) the current data from `neversion-db`:
- Create the services (Netflix, Disney Plus, etc.)
- Create the accounts per service
- Create the profiles per account
- Create the clients
- Create the subscriptions with their payment dates

#### Step 4: Readjust n8n workflow
See the **Workflow Adjustments** section below.

---

### Phase 2 -- Admin Panel
- Backend API (Java Spring Boot) over Supabase
- Web panel to manage: clients, accounts, profiles, subscriptions
- Manual status change (active -> suspended)
- "Dashboard" view with upcoming renewals

---

### Phase 3 -- Store + Payments
- Store where clients see available services and purchase their access
- Flow: client pays -> subscription activates automatically -> n8n sends credentials (Sprint 2)
- Automatic renewal or reminder before expiration

---
