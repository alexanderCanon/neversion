# Testing Standards

Standards and conventions for the Neversion API test suite. All tests live under `src/test/java/com/neversion/api/`.

---

## 1. Unit Tests (UT)

| Rule | Detail |
|------|--------|
| **Suffix** | `*UT.java` |
| **Runner** | `@ExtendWith(MockitoExtension.class)` |
| **Spring context** | None. No `@SpringBootTest`, no `@Autowired`. |
| **Dependencies** | `@Mock` for every collaborator; manual instantiation in `@BeforeEach`. |
| **Assertions** | AssertJ only (`assertThat`, `assertThatThrownBy`). |
| **Target layer** | Application services (`application/service/`) and domain services (`domain/service/`). |

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAccountService unit tests")
class CreateAccountServiceUT {

    @Mock private AccountRepositoryPort accountRepositoryPort;

    private CreateAccountService sut;

    @BeforeEach
    void setUp() {
        sut = new CreateAccountService(accountRepositoryPort);
    }
}
```

**Naming issue**: `SubscriptionServiceTest.java` does not follow the `*UT.java` convention. Rename to `SubscriptionServiceUT.java`. A correctly named copy already exists; delete the old file after verifying the copy is complete.

---

## 2. Integration Tests (IT)

| Rule | Detail |
|------|--------|
| **Suffix** | `*IT.java` |
| **Base class** | `extends BaseIntegrationTest` |
| **Container** | Testcontainers `PostgreSQLContainer("postgres:16-alpine")` with `@ServiceConnection` |
| **Profile** | `@ActiveProfiles("test")` — inherited from `BaseIntegrationTest` |
| **Schema** | Flyway migrations run automatically; `spring.jpa.hibernate.ddl-auto=validate` |
| **Assertions** | AssertJ. |

**Subtypes**:

### 2a. Repository IT

Validates JPA adapter behavior against a real PostgreSQL instance: save, find, soft-delete, uniqueness constraints.

```java
@SpringBootTest
class AccountRepositoryIT extends BaseIntegrationTest {
    @Autowired private SpringDataAccountRepository repository;
}
```

### 2b. Controller IT

Full-stack HTTP tests using `MockMvc` over a real application context.

```java
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIT extends BaseIntegrationTest {
    @Autowired private MockMvc mockMvc;
}
```

---

## 3. Security Tests

Security integration tests verify RBAC rules at the HTTP layer. They belong in Controller IT files or in dedicated `SecurityFilterChainIT`-style classes.

| Scenario | Approach |
|----------|----------|
| No token | `mockMvc.perform(get(...))` with no `Authorization` header |
| Invalid JWT | Hard-coded structurally valid JWT signed with wrong secret |
| Valid admin JWT | Mint a real HS256 JWT using the test secret from `application-test.yaml` |

The test secret is: `test-secret-key-for-testing-purposes-only-min-256-bits!!`

Use a shared utility method (see `reusable-test-templates.md`) to generate valid JWTs in tests rather than duplicating HMAC logic.

---

## 4. Naming Conventions

### File names

| Type | Pattern | Example |
|------|---------|---------|
| Unit test | `<ServiceClass>UT.java` | `CreateAccountServiceUT.java` |
| Integration test | `<Subject>IT.java` | `AccountControllerIT.java` |
| Base class | `Base*Test.java` | `BaseIntegrationTest.java` |

### Method names

Pattern: `methodUnderTest_scenario_expectedBehavior`

```java
@Test
@DisplayName("create - should throw BusinessRuleException when renewal date is null")
void create_shouldThrowBusinessRuleException_whenRenewalDateIsNull() { ... }
```

Every `@Test` must have a `@DisplayName` that mirrors the method name in human-readable form.

### Test body structure

Every test method uses three clearly labeled sections:

```java
// Given
Account account = buildAccount(SaleMode.BY_PROFILE);

// When
Account result = createAccountService.create(account);

// Then
assertThat(result).isNotNull();
```

For exception tests, `// When / Then` can be combined with `assertThatThrownBy`.

### Nested classes

Group related tests with `@Nested` + `@DisplayName` matching the method under test:

```java
@Nested
@DisplayName("create")
class Create { ... }

@Nested
@DisplayName("getById")
class GetById { ... }
```

---

## 5. Forbidden Patterns

| Anti-pattern | Why |
|-------------|-----|
| **H2 in-memory database** | Dialect and behavior differences mask real bugs. Testcontainers with PostgreSQL 16 is mandatory. |
| **`@Autowired` on fields in test classes** | Constructor injection or `@Autowired` on methods is acceptable in ITs; field injection is tolerated only on `MockMvc` and repository beans in ITs where Spring manages the lifecycle. Never in UTs. |
| **Mockito in domain layer tests** | Domain services that depend only on value objects should be tested with real inputs, not mocks. Mocking is for port interfaces only. |
| **Coverage percentage as a quality gate** | Coverage metrics inform — they do not replace scenario-driven test design. A 90% coverage number with no boundary tests is worse than 60% with invariant coverage. |
| **`@MockBean` in unit tests** | `@MockBean` loads a Spring context. Use `@Mock` + `MockitoExtension` instead. |
| **MapStruct-generated code in assertions** | The project uses manual mappers. Do not introduce MapStruct test utilities. |
| **Shared mutable test state** | Each test must construct its own fixture data. No static mutable state across tests. Use `@BeforeEach`, not `@BeforeAll`, for test data setup. |

---

## 6. Test Configuration

### `application-test.yaml`

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

supabase:
  jwt:
    secret: test-secret-key-for-testing-purposes-only-min-256-bits!!
```

### Maven Surefire

The `maven-surefire-plugin` is configured to pick up all four patterns:

```xml
<includes>
    <include>**/*Test.java</include>
    <include>**/*Tests.java</include>
    <include>**/*UT.java</include>
    <include>**/*IT.java</include>
</includes>
```

---

## 7. Builder Helpers

Each UT file should define private `build*()` methods that create domain objects with sensible defaults. This keeps the `// Given` section focused on the scenario variation:

```java
private Account buildAccount(SaleMode saleMode) {
    return Account.builder()
            .email("netflix@example.com")
            .password("pass123")
            .serviceId(SERVICE_ID)
            .renewalDate(LocalDate.now().plusDays(30))
            .plan("Premium")
            .saleMode(saleMode)
            .build();
}
```

If a builder helper is needed by multiple UT files in the same module, extract it into a package-private `TestFixtures` class within the same test package.
