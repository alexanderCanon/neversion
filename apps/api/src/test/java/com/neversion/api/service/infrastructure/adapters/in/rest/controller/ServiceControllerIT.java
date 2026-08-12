package com.neversion.api.service.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.service.application.port.in.ServiceUseCase;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.shared.domain.model.enums.CategoryType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ServiceController (EPIC-02).
 * Verifies RBAC, validation, and happy paths.
 * ServiceUseCase is mocked via @MockBean.
 */
import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("ServiceController IT — EPIC-02")
class ServiceControllerIT extends BaseWebIntegrationTest {


    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private static final UUID SERVICE_UUID = UUID.randomUUID();
    private static final UUID VENDOR_UUID = UUID.randomUUID();

    // ─── JWT helper ──────────────────────────────────────────────────────────

    private String buildJwt(String role) throws Exception {
        byte[] secret = JWT_SECRET.getBytes();
        JWSSigner signer = new MACSigner(secret);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("supabase-user-" + UUID.randomUUID())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                .claim("app_metadata", Map.of("role", role))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    private Service mockService() {
        return Service.builder()
                .id(1L).uuid(SERVICE_UUID)
                .name("Netflix Premium")
                .category(CategoryType.STREAMING)
                .priceProfile(BigDecimal.valueOf(45))
                .priceFull(BigDecimal.valueOf(150))
                .durationDays(30)
                .maxProfiles(5)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Map<String, Object> validRequestBody() {
        return Map.of(
                "name", "Netflix Premium",
                "category", "streaming",
                "priceProfile", 45.00,
                "priceComplete", 150.00,
                "durationDays", 30,
                "maxProfiles", 5
        );
    }

    // ─── US-017: Create ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("US-017 — POST /api/v1/services")
    class CreateTests {

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void create_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(post("/api/v1/services")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 when caller is CLIENT")
        void create_shouldReturn403_whenClient() throws Exception {
            mockMvc.perform(post("/api/v1/services")
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void create_shouldReturn400_whenMissingFields() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("name", "Netflix"));
            mockMvc.perform(post("/api/v1/services")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 201 when VENDOR creates a valid service")
        void create_shouldReturn201_whenVendor() throws Exception {
            when(serviceUseCase.create(any(), anyString())).thenReturn(mockService());

            mockMvc.perform(post("/api/v1/services")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("Netflix Premium"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }
    }

    // ─── US-019: Toggle status ───────────────────────────────────────────────

    @Nested
    @DisplayName("US-019 — PATCH /api/v1/services/{id}/status")
    class ToggleTests {

        @Test
        @DisplayName("should return 401 without JWT")
        void toggle_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(patch("/api/v1/services/{id}/status", SERVICE_UUID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 200 when VENDOR toggles their service")
        void toggle_shouldReturn200_whenOwner() throws Exception {
            Service toggled = mockService();
            toggled.setIsActive(false);
            when(serviceUseCase.toggleStatus(any(), anyString())).thenReturn(toggled);

            mockMvc.perform(patch("/api/v1/services/{id}/status", SERVICE_UUID)
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }

    // ─── US-020: Vendor panel list ───────────────────────────────────────────

    @Nested
    @DisplayName("US-020 — GET /api/v1/services/vendor/{vendorUuid}")
    class VendorListTests {

        @Test
        @DisplayName("should return 401 without JWT")
        void list_shouldReturn401_withoutToken() throws Exception {
            mockMvc.perform(get("/api/v1/services/vendor/{uuid}", VENDOR_UUID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 200 with all vendor services when authenticated as VENDOR")
        void list_shouldReturn200_whenVendor() throws Exception {
            when(serviceUseCase.listByVendor(any(), any(), any(), anyString()))
                    .thenReturn(List.of(mockService(), mockService()));

            mockMvc.perform(get("/api/v1/services/vendor/{uuid}", VENDOR_UUID)
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("should accept optional category and isActive filters")
        void list_shouldAcceptFilters() throws Exception {
            when(serviceUseCase.listByVendor(any(), any(), any(), anyString()))
                    .thenReturn(List.of(mockService()));

            mockMvc.perform(get("/api/v1/services/vendor/{uuid}", VENDOR_UUID)
                            .param("category", "STREAMING")
                            .param("isActive", "true")
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    // ─── US-021: Public store ─────────────────────────────────────────────────

    @Nested
    @DisplayName("US-021 — GET /api/v1/services/store/{vendorUuid} (public)")
    class StoreListTests {

        @Test
        @DisplayName("should return 200 without any JWT (public endpoint)")
        void store_shouldReturn200_withoutToken() throws Exception {
            when(serviceUseCase.listActive(any())).thenReturn(List.of(mockService()));

            mockMvc.perform(get("/api/v1/services/store/{uuid}", VENDOR_UUID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].isActive").value(true));
        }

        @Test
        @DisplayName("should return empty list when vendor has no active services")
        void store_shouldReturnEmpty_whenNoActiveServices() throws Exception {
            when(serviceUseCase.listActive(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/services/store/{uuid}", VENDOR_UUID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
