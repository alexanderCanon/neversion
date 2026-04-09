# AGENTS.md - Neversion Panel Backend

This file can iterate and change over time, so always check it before starting any task.

## Project Overview
- **Framework:** Spring
- **Type**: Spring Boot 3.5.10 REST API
- **Language**: Java 17
- **Build Tool**: Maven (./mvnw)
- **Architecture**: Hexagonal (Ports & Adapters)
- **Database**: PostgreSQL (Supabase), PostgreSQL Testcontainers for IT (Integration Tests)
- **ORM:** Spring Data JPA + Hibernate
- **Validation:** Spring Boot Starter Validation (Jakarta)
- **Authentication:** Supabase Auth (JWT)
- **Security:** Spring Security 6
- **Serialization:** Spring Boot Starter JSON (Jackson)
- **Boilerplate:** Lombok (Builder, Getter and Setter annotations)
---

## Build Commands

### Run Application
```bash
./mvnw spring-boot:run
```

### Build
```bash
./mvnw clean package -DskipTests    # Build without tests
./mvnw clean install                 # Build and run all tests
```

### Run Tests
```bash
./mvnw test                          # Run all tests
./mvnw test -Dtest=ClassName         # Run single test class
./mvnw test -Dtest=ClassName#method  # Run single test method
./mvnw test -Dtest="ClassName1,ClassName2"  # Run multiple test classes
```

### Other Commands
```bash
./mvnw compile                       # Compile only
./mvnw verify                        # Full verification including integration tests
```

---

## Code Style Guidelines

### Project Structure
Follow `SPEC.md` - Hexagonal architecture with these layers:
```
<feature>/
├── domain/
│   ├── model/          # Domain entities, enums
│   ├── port/out/      # Repository interfaces (ports)
│   └── service/       # Domain services (business logic)
├── application/
│   ├── port/in/       # Use case interfaces
│   └── service/       # Application services (use case implementations). Orchestration only. No business logic.
└── infrastructure/
    ├── adapters/in/   # REST controllers, DTOs, mappers
    └── adapters/out/  # JPA entities, repositories, persistence mappers
```

### Dependency Injection
- **Use constructor injection only**
- Never use `@Autowired` on fields

### Naming Conventions
- **Packages**: lowercase (e.g., `com.neversion.panel.product`)
- **Classes**: PascalCase (e.g., `ProductService`, `ProductEntity`)
- **Interfaces**: `<Name>Port`, `<Name>UseCase`, `<Name>Repository`
- **DTOs**: `<Name>Request`, `<Name>Response`
- **Controllers**: `<Feature><Operation>Controller` (e.g., `ProductPostController`)
- **Tests**: `<ServiceName>UT` (e.g., `CreateProductServiceUT`) for Unit Tests, and so on.
- **Methods**: camelCase
- **Constants**: UPPER_SNAKE_CASE

### DTOs and Records
- Use **Java Records** for immutable DTOs
- Use `@Builder` with Lombok for mutable DTOs
- Validate with Jakarta Validation (`@NotBlank`, `@NotNull`, etc.)

### Domain Model
- Use Lombok `@Builder`, `@Getter`, `@Setter`
- **Well commented code**
- Domain must NOT depend on JPA entities
- Use `@Version` for optimistic locking on entities if is necessary

### Mappers
- Separate mapper classes for each conversion:
  - `dto/RequestMapper` - DTO to Domain
  - `dto/ResponseMapper` - Domain to DTO
  - `persistence/EntityMapper` - Entity to Domain
- **No MapStruct** - use manual mappers with Builder

### Database Access
- Use `getReferenceById()` for validation optimization (not loading entity)
- Use `@Query` for custom JPQL when needed
- Apply soft delete with `@SQLDelete` and `@Where`

### Error Handling
- Use `GlobalExceptionHandler` with `@ControllerAdvice`
- Return proper HTTP status codes (200, 201, 400, 404, 500)
- Throw domain exceptions (`ResourceNotFoundException`, etc.)

### REST API
- Prefix all endpoints with `/api/v1/`
- Use `@Valid` on request bodies
- Use `@RequestBody` for input, `ResponseEntity<T>` for output

---

## Testing Guidelines

### Framework
- **JUnit 5** with **Mockito**
- **AssertJ** for assertions

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Test
    @DisplayName("create - should return saved product")
    void create_shouldReturnSavedProduct() {
        // Given
        Product input = Product.builder()
            .name("Netflix")
            .category(CategoryType.PLATAFORMA)
            .build();

        when(productRepositoryPort.save(input)).thenReturn(persisted);

        // When
        Product result = createProductService.create(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Netflix");
    }
}
```

### Test Naming
- Follow: `<method>_<scenario>_<expected_result>`
- Use `@DisplayName` for descriptive test names
- Use `@BeforeEach` for test setup

### Test Configuration
- Profile: `test` (activated via `application-test.yaml`)
- Database: PostgreSQL Testcontainers for IT (Integration Tests)
- Use `@ActiveProfiles("test")` if manual activation needed

---

## Security
- Spring Security 6
- CSRF disabled (for API-only)
- Supabase Auth (JWT) for authentication
- Never expose credentials in responses
---

## Important Notes
1. **No duplicate product names** - validate before saving
2. **Inventory references Product by ID only** - no bidirectional relationships
3. **Product cannot be deleted if has active inventories**
4. **Duration discounts**: 90+ days = 3% monthly discount
5. **Validate all inputs with Jakarta Validation annotations**
6. **Soft delete:** The entity uses SQLDelete and SQLRestriction
