# General Description
This files describes what endpoint are public and what are admin-only.
It can be updated as the project evolves.

**Act as a Senior Spring Boot Security Expert and Backend Developer.**  
We are using **Spring Boot 3.5.10** and **Spring Security** to secure our REST API. For authentication and identity management, we are using **Supabase Auth**, main task is to implement the security layer (OAuth2 Resource Server with JWT) based on the strict architectural guidelines and Role-Based Access Control (RBAC).

### 1. Architectural Context & Security Strategy
- **Authentication Provider:** Supabase. The backend will act as an OAuth2 Resource Server validating JWTs symmetrically (HS256) or via JWKS using the Supabase project secret/URL.
- **Role Extraction:** Supabase stores custom user roles (e.g., 'admin') inside the JWT claims, specifically under `app_metadata.role` or `user_metadata.role`. You must implement a custom `JwtAuthenticationConverter` to extract this and map it to Spring's `GrantedAuthority` (e.g., `ROLE_ADMIN`).
- **Guest Users (Security by Obscurity - Path A):** Unregistered users (Guests) do NOT have a JWT. They interact with the system using hard-to-guess UUIDs (v4). Therefore, endpoints modifying a specific guest resource (like `GET /api/reservations/{uuid}` or `PUT /api/reservations/{uuid}`) must be publicly accessible at the filter level, assuming the controller or service layer handles the "UUID possession" authorization.

### 2. RBAC Access Matrix
Configure the `SecurityFilterChain` exactly as follows:

**Public Endpoints (`permitAll()`):**
- [x] `GET /api/products/**` (Catalog is public)
- [x] `GET /api/inventory/**`
- [x] `POST /api/users-guests` (Create guest profile)
- [x] `POST /api/reservations` (Create reservation)
- [x] `GET, PUT /api/reservations/{id}` (Guest viewing/editing their reservation via UUID)
- [x] `GET, PUT /api/users-guests/{id}` (Guest viewing/editing their profile via UUID)
- [x] Swagger/OpenAPI docs (`/v3/api-docs/**`, `/swagger-ui/**`)

**Admin-Only Endpoints (`hasRole('ADMIN')`):**
- [x] `POST, PUT, DELETE /api/products/**`
- [x] `POST, PUT, DELETE /api/inventory/**`
- [x] `GET, POST, PUT, DELETE /api/accounts/**` (Raw materials/credentials are strictly admin-only)
- [x] `DELETE /api/reservations/**`
- [x] `DELETE /api/users-guests/**`

### 3. Deliverables Requested
Please provide the following exact implementation details:
1. **Dependencies:** The required Maven/Gradle dependencies for Spring Boot 3 OAuth2 Resource Server if not already included.
2. **Configuration properties:** The setup required in `application.yml` or `application-dev.yml` for Supabase JWT validation. There is a placeholder already with the JWT Secret Key.
3. **`SupabaseJwtAuthConverter.java`:** The custom converter class to extract the Admin role from the Supabase JWT.
4. **`SecurityConfig.java`:** The main security configuration class utilizing the modern HTTP Security builder (lambda DSL), enabling CORS, disabling CSRF (since it's a stateless REST API), and implementing the RBAC matrix defined above. Defined at src/main/java/com/neversion/panel/config/SecurityConfig.java

### 4. Additional Notes
We can add Logger to see what Claims are extracted from the JWT each time a request is made.

Write clean, production-ready, and well-commented Java code.