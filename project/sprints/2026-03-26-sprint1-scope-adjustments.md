# Documentation Adjustment Notice: Sprint 1 Scope Reduction
**Date:** 2026-03-26
**Target Files to logically amend:** `docs/sprints/sprint1-neversion-system.md`, `docs/modules/reservations-orders.md`

## 🔍 Observation from User Stories (2026-03-25)
The latest User Stories explicitly state: *"it is possible that entities/repositories called reservations and orders exist; these are not important for Sprint 1 because there is no operational store. What is needed is administration/management"*.

## 🛠️ Required Adjustments to our Current Architecture Docs
Based on this story, our previous documentation incorrectly included a manual "checkout" and "receipt validation" flow in Sprint 1. The documentation must now be treated with the following adjustments:

1. **Complete Exclusion of Reservations & Orders:** 
   - The entire `reservations` and `orders` entities, along with S3 receipt uploads, are officially pushed to **Sprint 2**.
2. **Simplified Direct Subscription Workflow:** 
   - Sprint 1 strictly focuses on migrating the Admin's manual "Notion" tables into the backend database.
   - The Admin workflow is direct: Create Product -> Create Inventory -> Create Master Account -> Create Guest User -> **Directly Create Subscription** (Linking Guest to an Account Slot).
   - The `order_id` in the `subscriptions` table will remain nullable or unused during Sprint 1.
