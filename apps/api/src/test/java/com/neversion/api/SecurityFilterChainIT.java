package com.neversion.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for the {@code SecurityFilterChain}.
 * <p>
 * Boots the full Spring context with a real PostgreSQL container and verifies
 * that the security rules are enforced at the HTTP layer:
 * <ul>
 * <li>Public endpoints return 200 without any token</li>
 * <li>Protected endpoints return 401 without a token</li>
 * <li>Protected endpoints return 401 with an invalid/malformed JWT</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
@DisplayName("SecurityFilterChain IT — endpoint protection rules")
class SecurityFilterChainIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Public endpoints (no token needed) ─────────────────────────────

    @Nested
    @DisplayName("Public endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /api/v1/services/store/{uuid} - should not require auth (US-021, returns 404 for unknown vendor)")
        void servicesStore_shouldBePublic() throws Exception {
            mockMvc.perform(get("/api/v1/services/store/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /actuator/health - should return 200 without token")
        void actuatorHealth_shouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /v3/api-docs - should return 200 without token (OpenAPI)")
        void apiDocs_shouldBePublic() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());
        }
    }

    // ── Protected endpoints (token required) ───────────────────────────

    @Nested
    @DisplayName("Protected endpoints — no token")
    class ProtectedEndpointsNoToken {

        @Test
        @DisplayName("POST /api/v1/services - should return 401 without token")
        void createService_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(post("/api/v1/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Netflix",
                                "maxProfiles": 5
                            }
                            """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/accounts - should return 401 without token (admin-only)")
        void getAccounts_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(get("/api/v1/accounts"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/subscriptions - should return 401 without token (admin-only)")
        void listSubscriptions_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(get("/api/v1/subscriptions"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/clients - should return 401 without token (admin-only)")
        void clients_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(get("/api/v1/clients"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /actuator/prometheus - should return 401 without token (SUPER_ADMIN only)")
        void actuatorPrometheus_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(get("/actuator/prometheus"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /actuator/prometheus - should return 401 without auth token")
        void actuatorPrometheus_shouldReturn401_withInvalidScrapeToken() throws Exception {
            mockMvc.perform(get("/actuator/prometheus")
                    .header("Authorization", "Bearer wrong-token"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Invalid JWT ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Protected endpoints — invalid JWT")
    class ProtectedEndpointsInvalidJwt {

        @Test
        @DisplayName("POST /api/v1/accounts - should return 401 with malformed JWT")
        void createAccount_shouldReturn401_withMalformedJwt() throws Exception {
            mockMvc.perform(post("/api/v1/accounts")
                    .header("Authorization", "Bearer this.is.not.a.valid.jwt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "email": "test@example.com",
                                "password": "pass123",
                                "serviceId": "00000000-0000-0000-0000-000000000001",
                                "plan": "Premium",
                                "saleMode": "BY_PROFILE",
                                "renewalDate": "2026-04-30"
                            }
                            """))
                    .andExpect(status().isUnauthorized());
        }
    }
}
