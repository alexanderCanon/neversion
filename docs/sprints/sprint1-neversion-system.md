# Sprint 1: Functional MVP (Manual Operations)

## 1. Sprint 1 Business Scope
The primary goal of Sprint 1 is to digitize and manage the Administrator's manual, Excel-based operation. 

* **Admin Operations (Backoffice Focus):** The Admin Panel is the core deliverable. The Admin will manually validate uploaded receipts, procure the physical accounts, enter them into the system, and manually assign available slots to customers via Subscriptions. Automation is intentionally kept low to prevent operational errors during launch.
* **Customer Operations (Guest Mode Focus):** Customers operate via direct links or a basic storefront. They act strictly as `user_guests` (without a login profile), construct a cart over a Reservation, and manually upload their payment receipts. 

## 2. Product Categories
The system manages various digital products classified into the following categories:
- **Streaming Platforms & Subscriptions**
- **Digital Services & Software Licenses**
- **Top-ups & Gift Cards**

## 3. Actors & Capabilities

**Actor: ADMIN**
The admin has full control of the business via the Backoffice panel:
- **Catalog & Inventory:** Full CRUD over conceptual `products` and sellable `inventory` variants.
- **Physical Inventory:** Full CRUD over `accounts` (master credentials bought from providers) and their respective `account_slots`.
- **Validation:** Receives and manually validates S3 receipts uploaded by guests to convert `Reservations` into `Orders`.
- **Fulfillment & Subscriptions:** Manually maps `Orders` to `User_Guests`, assigns them an available `account_slot`, and delivers the credentials. Handles subscriptions renewals manually.
- **Customer Migration:** Progressive migration of records from legacy Excel spreadsheets.

**Actor: CUSTOMER (User Guest)**
- **Explore:** Customers can view products and granular variants (type, duration, price).
- **Checkout:** Customers enter their guest data (name, email, phone) to create a `Reservation`. 
- **Hold & Payment:** The reservation freezes the price and gives the customer a 60-minute window to manually upload their payment receipt.
- *Note: Customer profiles, login, and self-service portals are out-of-scope for Sprint 1.*

## 4. Key Relationships & Entities

- **Product ↔ Inventory Variant**: 1-to-Many.
- **Inventory ↔ Account**: 1-to-Many (Accounts are procured based on inventory lines).
- **Account ↔ Account Slot**: 1-to-Many (For FAMILY accounts).
- **Account Slot / Account ↔ Subscription**: 1-to-1 Active Assignment (Individual exclusivity applies).
- **User Guest ↔ Subscription**: 1-to-Many (A guest can have multiple active subscriptions).
- **Reservation ↔ Order**: 1-to-1 (An Order is strictly the finalized, successfully-paid state of a Reservation).
- **Order ↔ Subscription**: 1-to-Many (One order chunk can fulfill multiple subscriptions).

## 5. Lifecycle & Business Constraints
- **Inventory Autonomy:** Variants and Access Credentials function as independent domains. While they are linked to a Product, they must be managed as standalone entities.
- **Reservation-First Requirement:** An Order cannot be instantiated directly. Every Order must originate from a pre-existing Reservation.
- **Conversion Logic:** A Reservation must generate its details first. Only upon successful upload and Admin validation is the record promoted to an Order, maintaining a mandatory reference to the original Reservation.

---

## 6. Technical Requirements & Refactoring Guidelines

### 6.1. Soft Delete Implementation
- Implement soft delete using the `is_active` field across products and related entities. We need to deactivate products and variants without hard-deleting them. The frontend should display these as "Product unavailable".
- **Cascade Logic**: Deactivating a `Product` must automatically deactivate all of its associated `Inventory` variants.
- Entities should be restorable via direct database updates to `is_active`.

### 6.2. API & Data Access
- **Order Retrieval**: Implement `GetController` and `DTOResponse` for the `Order` entity to allow both Admin and Customer visibility.
- **Persistence Strategy**: Review JPA usage; prioritize `saveAndFlush` over `save` where immediate database synchronization is required for business logic sequence processing.

### 6.3. Architectural Alignment
- **Security**: Decentralize security configurations. Move logic from the global configuration to entity-specific infrastructure layers (`**/infrastructure/config`).
- **Domain Logic**: Relocate business-critical logic (e.g. manual Discount calculations) from the Application layer to the Domain layer (`domain/service`).
- **Exception Handling**: Move exception handling to the Infrastructure layer. Utilize custom exceptions defined in `com.neversion.api.exceptions`.
- **Transactions**: Audit the Application layer to ensure `@Transactional` is applied correctly to methods requiring atomicity.

### 6.4. Testing & Quality Standards
- **Naming Conventions**: Append specific suffixes to test classes:
  - Unit Tests: `*UnitTest` or `*UT`.
  - Integration/Slicing Tests: Use descriptive suffixes like `*IT`.
- **Documentation**: Ensure code is modular, well-organized, and includes meaningful comments for complex logic.

---

## 7. Explicitly Excluded (Ignore)
- **Profile Module:** Ignore the `profile` repository/package (Customer authentication and portals are Sprint 2).
- **Automation Logic:** Automated combo discounts, automated slot assignment algorithms, and automated payment gateways are deferred to Sprint 2.
- **Env files:** Ignore `.env` / `.env.template` files.
