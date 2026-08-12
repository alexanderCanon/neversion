package com.neversion.api.auth.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.user.application.port.in.GetCurrentUserContextUseCase;
import com.neversion.api.user.application.port.in.RegisterClientUseCase;
import com.neversion.api.user.application.port.in.RegisterVendorUseCase;
import com.neversion.api.user.domain.model.RegisterVendorResult;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for POST /api/v1/auth/vendors (US-012).
 * <p>
 * Verifies RBAC rules and the full HTTP layer. The use case is mocked
 * via @MockBean to isolate from the database.
 * <p>
 * JWT generation uses Nimbus JOSE (already on classpath via
 * spring-boot-starter-oauth2-resource-server) — no extra dependencies.
 */
import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("AuthController IT — POST /api/v1/auth/vendors")
class RegisterVendorIT extends BaseWebIntegrationTest {


    // Must match application-test.yaml supabase.jwt.secret
    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    // ─── JWT builder (Nimbus, already on classpath) ───────────────────────────

    /**
     * Builds a signed HS256 JWT with the given role in app_metadata.
     * Matches the claim extraction logic in SupabaseJwtAuthConverter.
     */
    private String buildJwt(String role) throws Exception {
        byte[] secret = JWT_SECRET.getBytes();
        JWSSigner signer = new MACSigner(secret);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("auth|test-" + UUID.randomUUID())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                .claim("app_metadata", Map.of("role", role))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    private String validBody() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "email", "newvendor@tienda.com",
                "password", "secret123!",
                "storeName", "Mi Tienda Digital"
        ));
    }

    // ─── 401: no token ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("401 — No authentication")
    class NoAuthTests {

        @Test
        @DisplayName("should return 401 when no JWT is provided")
        void registerVendor_shouldReturn401_whenNoToken() throws Exception {
            mockMvc.perform(post("/api/v1/auth/vendors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── 403: wrong role ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("403 — Wrong role")
    class ForbiddenTests {

        @Test
        @DisplayName("should return 403 when caller has role VENDOR")
        void registerVendor_shouldReturn403_whenCallerIsVendor() throws Exception {
            mockMvc.perform(post("/api/v1/auth/vendors")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 when caller has role CLIENT")
        void registerVendor_shouldReturn403_whenCallerIsClient() throws Exception {
            mockMvc.perform(post("/api/v1/auth/vendors")
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── 400: validation ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("400 — Validation errors")
    class ValidationTests {

        @Test
        @DisplayName("should return 400 when email is missing")
        void registerVendor_shouldReturn400_whenEmailMissing() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "storeName", "Mi Tienda Digital"
            ));
            mockMvc.perform(post("/api/v1/auth/vendors")
                            .header("Authorization", "Bearer " + buildJwt("super_admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when storeName is missing")
        void registerVendor_shouldReturn400_whenStoreNameMissing() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", "vendor@test.com"
            ));
            mockMvc.perform(post("/api/v1/auth/vendors")
                            .header("Authorization", "Bearer " + buildJwt("super_admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void registerVendor_shouldReturn400_whenEmailInvalid() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", "not-an-email",
                    "storeName", "Mi Tienda"
            ));
            mockMvc.perform(post("/api/v1/auth/vendors")
                            .header("Authorization", "Bearer " + buildJwt("super_admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── 201: happy path ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("201 — Successful registration")
    class SuccessTests {

        @Test
        @DisplayName("should return 201 with vendor details when SUPER_ADMIN registers vendor")
        void registerVendor_shouldReturn201_whenSuperAdminRegisters() throws Exception {
            RegisterVendorResult mockResult = new RegisterVendorResult(
                    "auth-vendor-123",
                    UUID.randomUUID(),
                    "Mi Tienda Digital",
                    "newvendor@tienda.com"
            );

            when(registerVendorUseCase.register(any())).thenReturn(mockResult);

            mockMvc.perform(post("/api/v1/auth/vendors")
                            .header("Authorization", "Bearer " + buildJwt("super_admin"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.vendorUuid").isNotEmpty())
                    .andExpect(jsonPath("$.externalId").isNotEmpty())
                    .andExpect(jsonPath("$.storeName").value("Mi Tienda Digital"))
                    .andExpect(jsonPath("$.email").value("newvendor@tienda.com"));

        }
    }
}
