# Documentation Adjustment Notice: Dashboard & Notion Migration
**Date:** 2026-03-26
**Target Files to logically amend:** `docs/api-contracts/dashboard.md`, `docs/modules/subscriptions.md`

## 🔍 Observation from User Stories (2026-03-25)
The User Stories highlight a specific view representing the Admin's current Notion setup:
A breakdown by **Digital Service** (Streaming) -> **Master Account** (Email / Pass / Date) -> **Table of occupied slots** (Customer, Phone, Profile, PIN, Payment Date).

Furthermore, the stories dictate specific critical needs for the dashboard:
1. Control when the master account expires.
2. Identify customers whose renewal dates are near (expiring subscriptions).
3. See which profiles (slots) are available and occupied.

## 🛠️ Required Adjustments to our Current Architecture Docs
Our API and Data models already support this, but the documentation and frontend implementation focus must shift to prioritize this specific exact view.

1. **Dashboard Aggregation:** The `SubscriptionDashboardDTO` in our API Contracts must mirror this Notion view cleanly, grouping subscriptions under their parent master accounts.
2. **Enforcement of Calculated Enums:** The `EXPIRING_SOON` logic (for subscriptions) and `account_availability` logic (for accounts) defined in `docs/enums/2026-03-24-enums.md` are now the most critical functional requirements for Sprint 1's UI, as they solve the Admin's operational pain points.
