# Sprint 2: Automated Storefront & Customer Portals

## 1. Sprint 2 Business Scope
The primary goal of Sprint 2 is to launch the fully-fledged, customer-facing E-Commerce storefront and introduce high levels of automation.

While Sprint 1 focused on the Administrator managing everything manually in the Backoffice as an MVP, Sprint 2 shifts the focus to self-service portals, automated assignments, and a streamlined integration for the end-user.

## 2. Key Implementations

### A. Profiles & Authentication
- **Customer Accounts:** Introduce the `profiles` table as a public schema mirror tied directly to **Supabase Auth**.
- **User Transition:** Move away from purely relying on checking out as a `user_guest`. Allow users to register and maintain an authenticated session.
- **Customer Portal:** Build a frontend module that allows authenticated customers to log in and review their active subscriptions, purchase history, and renewal dates, eliminating the need to ask via WhatsApp.

### B. Payment Gateways
- **Automated Processing:** Move beyond the manual upload of S3 payment receipts. Implement automated payment gateways capable of instantly creating and validating `Order` payments.

### C. Automated Fulfillment & Slot Assignment
- **Algorithmic Fulfillment:** Instead of the Admin manually mapping an Order to an available `Account Slot`, the backend will execute an automated assignment process.
- **Immediate Credential Delivery:** Once the payment gateway confirms the transaction, the backend will auto-generate the `Subscription`, assign an `AVAILABLE` `Profile` (or `INDIVIDUAL` Account), and immediately present the streaming credentials / PINs to the user in their newly built Profile Portal or via automated email.

### D. Automated System Logic & Discounts
- **Combo Discounts Model:** Introduce the algorithmic combo discount engine previously defined in the business requirements:
  - If the cart has **1 service**: Full catalog price (0% discount).
  - If the cart has **2 or more services**: Apply an automatic **2%** discount (or a client-defined tiered percentage) over the applicable products subtotal.
  - *Data layer note:* The discount calculations will be structurally persisted and recorded in `reservations` and `orders` (e.g., `discount_total`).

## 3. Impact on Existing Sprint 1 Modules
- The `User_Guest` flow will remain intact for customers who want to check out very quickly, but the system will encourage creating a `Profile`.
- The `discount` field will transition from being an admin-only manual lever to automatically calculated via the Domain services.
- Subscriptions will see a massive influx of system-generated `created_by` tags instead of admin intervention.

## 4. Frontend Deliverable Focus
- **Customer Facing Application:** The E-Commerce storefront will be entirely operational, pulling real-time data from `products` and `inventory`.
- **Checkout Experience Redesign:** Incorporating dynamic 2% combo discount UI hints.
