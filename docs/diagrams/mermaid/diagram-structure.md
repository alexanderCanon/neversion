# Backend and Frontend Interoperability Diagram

This flowchart outlines the authentication and communication structure for the Neversion system, separating the customer-facing Store from the protected Admin management system.

```mermaid
graph TD
    subgraph STORE [PROJECT 1: STORE]
        A1[Angular 17 Client]
        A2[Login and Data RLS]
        A3[Supabase Auth / DB]
        A1 --> A2
        A2 --> A3
    end

    subgraph ADMIN [PROJECT 2: ADMIN]
        B1[Admin Angular 17]
        B2[Backend Spring Boot Java]
        B3[(Supabase DB)]
        B1 -- "3. Request + Bearer JWT" --> B2
        B2 -- "4. Validate Token & Query" --> B3
    end

    B1 -- "1. Login Client SDK" --> A3
    A3 -- "2. Returns JWT Token" --> B1

    %% Styling
    style A1 fill:#fff,stroke:#333
    style A2 fill:#ccc,stroke:#333
    style A3 fill:#fff,stroke:#f60,stroke-width:2px
    style B1 fill:#e1f5fe,stroke:#039be5,stroke-width:2px
    style B2 fill:#e8f5e9,stroke:#4caf50,stroke-width:2px
    style B3 fill:#fff,stroke:#333
```

## Communication Steps

1.  **Login Client SDK**: The Admin Panel uses the Supabase SDK to authenticate against the primary Supabase Auth instance.
2.  **Returns JWT Token**: Supabase provides a JSON Web Token (JWT) upon successful authentication.
3.  **Request + Bearer JWT**: The Admin Panel sends API requests to the Java Spring Boot Backend, including the JWT in the `Authorization` header.
4.  **Validate Token & Query**: The Backend validates the token via Supabase's JWKS and proceeds to perform authorized queries on the database.
