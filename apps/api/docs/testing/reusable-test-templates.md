# Reusable Test Templates

Complete, compilable Java templates for each test type in the Neversion API. Copy, rename, and adapt to your module.

---

## 1. Unit Test Template (Application Service)

```java
package com.neversion.api.service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("DigitalServiceService unit tests")
class DigitalServiceServiceUT {

    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;

    private DigitalServiceService digitalServiceService;

    private static final UUID SERVICE_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        digitalServiceService = new DigitalServiceService(serviceRepositoryPort);
    }

    private Service buildService() {
        return Service.builder()
                .id(1L)
                .uuid(SERVICE_UUID)
                .name("Netflix")
                .maxProfiles(5)
                .build();
    }

    // ── create ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should save and return service when name is unique")
        void create_shouldSaveService_whenNameIsUnique() {
            // Given
            Service service = buildService();
            when(serviceRepositoryPort.existsByName("Netflix")).thenReturn(false);
            when(serviceRepositoryPort.save(service)).thenReturn(service);

            // When
            Service result = digitalServiceService.create(service);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Netflix");
            verify(serviceRepositoryPort).save(service);
        }

        @Test
        @DisplayName("should throw BusinessRuleException when name already exists (BR-17)")
        void create_shouldThrowBusinessRuleException_whenNameAlreadyExists() {
            // Given
            Service service = buildService();
            when(serviceRepositoryPort.existsByName("Netflix")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> digitalServiceService.create(service))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already exists");

            verify(serviceRepositoryPort, never()).save(any());
        }
    }

    // ── getById ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return service when found")
        void getById_shouldReturnService_whenFound() {
            // Given
            when(serviceRepositoryPort.findById(SERVICE_UUID))
                    .thenReturn(Optional.of(buildService()));

            // When
            Service result = digitalServiceService.getById(SERVICE_UUID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUuid()).isEqualTo(SERVICE_UUID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void getById_shouldThrowResourceNotFound_whenNotFound() {
            // Given
            when(serviceRepositoryPort.findById(SERVICE_UUID))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> digitalServiceService.getById(SERVICE_UUID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(SERVICE_UUID.toString());
        }
    }

    // ── getAll ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all services")
        void getAll_shouldReturnAllServices() {
            // Given
            when(serviceRepositoryPort.findAll()).thenReturn(List.of(buildService()));

            // When
            List<Service> result = digitalServiceService.getAll();

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // ── delete ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete service when found")
        void delete_shouldDeleteService_whenFound() {
            // Given
            when(serviceRepositoryPort.findById(SERVICE_UUID))
                    .thenReturn(Optional.of(buildService()));

            // When
            digitalServiceService.delete(SERVICE_UUID);

            // Then
            verify(serviceRepositoryPort).deleteById(SERVICE_UUID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void delete_shouldThrowResourceNotFound_whenNotFound() {
            // Given
            when(serviceRepositoryPort.findById(SERVICE_UUID))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> digitalServiceService.delete(SERVICE_UUID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
```

---

## 2. Repository Integration Test Template

```java
package com.neversion.api.account.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;

@SpringBootTest
@DisplayName("AccountRepository IT — persistence layer against PostgreSQL")
class AccountRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private AccountRepositoryPort accountRepositoryPort;

    @Autowired
    private SpringDataAccountRepository springDataAccountRepository;

    // Use a known service ID that exists after Flyway migration,
    // or insert prerequisite data in @BeforeEach.
    private Long existingServiceId;

    @BeforeEach
    void setUp() {
        // Clean slate for each test — truncate or use @Transactional + rollback
        springDataAccountRepository.deleteAll();

        // Insert prerequisite data if needed (e.g., a Service entity).
        // For this template, assume Flyway V1 seeds a service with id=1.
        existingServiceId = 1L;
    }

    private Account buildAccount() {
        return Account.builder()
                .email("repo-test@example.com")
                .password("pass123")
                .serviceId(existingServiceId)
                .renewalDate(LocalDate.now().plusDays(30))
                .plan("Premium")
                .saleMode(SaleMode.BY_PROFILE)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist account and assign UUID")
        void save_shouldPersistAccountAndAssignUuid() {
            // Given
            Account account = buildAccount();

            // When
            Account saved = accountRepositoryPort.save(account);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getUuid()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo("repo-test@example.com");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return account when exists")
        void findById_shouldReturnAccount_whenExists() {
            // Given
            Account saved = accountRepositoryPort.save(buildAccount());

            // When
            Optional<Account> found = accountRepositoryPort.findById(saved.getUuid());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("repo-test@example.com");
        }

        @Test
        @DisplayName("should return empty when not exists")
        void findById_shouldReturnEmpty_whenNotExists() {
            // When
            Optional<Account> found = accountRepositoryPort.findById(UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all non-deleted accounts")
        void findAll_shouldReturnAllNonDeletedAccounts() {
            // Given
            accountRepositoryPort.save(buildAccount());
            accountRepositoryPort.save(Account.builder()
                    .email("second@example.com")
                    .password("pass456")
                    .serviceId(existingServiceId)
                    .renewalDate(LocalDate.now().plusDays(15))
                    .plan("Basic")
                    .saleMode(SaleMode.FULL_ACCOUNT)
                    .build());

            // When
            List<Account> all = accountRepositoryPort.findAll();

            // Then
            assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        }
    }
}
```

---

## 3. Controller Integration Test Template

```java
package com.neversion.api.account.infrastructure.adapters.in.rest.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.neversion.api.BaseIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AccountController IT — HTTP integration tests")
class AccountControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── POST /api/v1/accounts ───────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/accounts")
    class CreateAccount {

        @Test
        @DisplayName("should return 201 with valid admin JWT and valid body")
        void create_shouldReturn201_withValidAdminJwt() throws Exception {
            String adminJwt = JwtTestHelper.mintAdminJwt();

            mockMvc.perform(post("/api/v1/accounts")
                            .header("Authorization", "Bearer " + adminJwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "password": "pass123",
                                        "serviceId": "00000000-0000-0000-0000-000000000001",
                                        "plan": "Premium",
                                        "saleMode": "BY_PROFILE",
                                        "renewalDate": "2026-05-30"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid", notNullValue()));
        }

        @Test
        @DisplayName("should return 401 without token")
        void create_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test@example.com",
                                        "password": "pass123"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void create_shouldReturn400_whenFieldsMissing() throws Exception {
            String adminJwt = JwtTestHelper.mintAdminJwt();

            mockMvc.perform(post("/api/v1/accounts")
                            .header("Authorization", "Bearer " + adminJwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /api/v1/accounts ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/accounts")
    class GetAccounts {

        @Test
        @DisplayName("should return 200 with admin JWT")
        void getAll_shouldReturn200_withAdminJwt() throws Exception {
            String adminJwt = JwtTestHelper.mintAdminJwt();

            mockMvc.perform(get("/api/v1/accounts")
                            .header("Authorization", "Bearer " + adminJwt))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 401 without token")
        void getAll_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(get("/api/v1/accounts"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
```

---

## 4. Security Integration Test Template

```java
package com.neversion.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Account Security IT — RBAC enforcement")
class AccountSecurityIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Public endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /api/v1/services - should return 200 without token")
        void services_shouldBePublic() throws Exception {
            mockMvc.perform(get("/api/v1/services"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Protected endpoints — no token")
    class NoToken {

        @Test
        @DisplayName("POST /api/v1/accounts - should return 401 without token")
        void createAccount_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"test@example.com","password":"pass"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Protected endpoints — valid admin JWT")
    class WithAdminJwt {

        @Test
        @DisplayName("GET /api/v1/accounts - should return 200 with admin JWT")
        void getAccounts_shouldReturn200_withAdminJwt() throws Exception {
            String adminJwt = JwtTestHelper.mintAdminJwt();

            mockMvc.perform(get("/api/v1/accounts")
                            .header("Authorization", "Bearer " + adminJwt))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Protected endpoints — JWT without admin role")
    class WithUserJwt {

        @Test
        @DisplayName("GET /api/v1/accounts - should return 403 with non-admin JWT")
        void getAccounts_shouldReturn403_withUserJwt() throws Exception {
            String userJwt = JwtTestHelper.mintJwt("user");

            mockMvc.perform(get("/api/v1/accounts")
                            .header("Authorization", "Bearer " + userJwt))
                    .andExpect(status().isForbidden());
        }
    }
}
```

---

## 5. JWT Test Helper Utility

Place this class at `src/test/java/com/neversion/api/JwtTestHelper.java`. It mints valid HS256 JWTs compatible with the `SupabaseJwtAuthConverter`.

```java
package com.neversion.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility to mint HS256 JWTs for integration tests.
 * Uses the test secret from application-test.yaml.
 * <p>
 * No external library required — pure JDK implementation.
 */
public final class JwtTestHelper {

    private static final String SECRET = "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private JwtTestHelper() {
    }

    /**
     * Mints a JWT with {@code app_metadata.role = "admin"} and a 1-hour expiry.
     */
    public static String mintAdminJwt() {
        return mintJwt("admin");
    }

    /**
     * Mints a JWT with the given role in {@code app_metadata.role}.
     *
     * @param role the role string (e.g., "admin", "user")
     * @return a signed HS256 JWT string
     */
    public static String mintJwt(String role) {
        return mintJwt(role, UUID.randomUUID().toString(), Instant.now().plusSeconds(3600));
    }

    /**
     * Mints a JWT with full control over subject, role, and expiry.
     *
     * @param role    the role string placed in {@code app_metadata.role}
     * @param subject the JWT subject (sub claim)
     * @param expiry  the expiration instant
     * @return a signed HS256 JWT string
     */
    public static String mintJwt(String role, String subject, Instant expiry) {
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");

        String payload = base64Url(String.format(
                "{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d,\"app_metadata\":{\"role\":\"%s\"}}",
                subject,
                Instant.now().getEpochSecond(),
                expiry.getEpochSecond(),
                role));

        String signingInput = header + "." + payload;
        String signature = hmacSha256(signingInput);

        return signingInput + "." + signature;
    }

    /**
     * Mints an expired JWT for testing rejection of expired tokens.
     */
    public static String mintExpiredJwt(String role) {
        return mintJwt(role, UUID.randomUUID().toString(),
                Instant.now().minusSeconds(3600));
    }

    private static String base64Url(String input) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
```

### Usage in tests

```java
// Admin JWT (ROLE_ADMIN)
String adminJwt = JwtTestHelper.mintAdminJwt();
mockMvc.perform(get("/api/v1/accounts")
        .header("Authorization", "Bearer " + adminJwt))
        .andExpect(status().isOk());

// Non-admin JWT (should get 403 on admin-only endpoints)
String userJwt = JwtTestHelper.mintJwt("user");
mockMvc.perform(get("/api/v1/accounts")
        .header("Authorization", "Bearer " + userJwt))
        .andExpect(status().isForbidden());

// Expired JWT (should get 401)
String expiredJwt = JwtTestHelper.mintExpiredJwt("admin");
mockMvc.perform(get("/api/v1/accounts")
        .header("Authorization", "Bearer " + expiredJwt))
        .andExpect(status().isUnauthorized());
```
