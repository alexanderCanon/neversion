package com.neversion.api.auth.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.user.application.port.in.GetCurrentUserContextUseCase;
import com.neversion.api.user.application.port.in.RegisterClientUseCase;
import com.neversion.api.user.application.port.in.RegisterVendorUseCase;
import com.neversion.api.user.domain.model.RegisterClientResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for POST /api/v1/auth/clients (US-013).
 * <p>
 * This endpoint is public — no JWT required.
 * Use cases are mocked to isolate the web + security layer.
 */
import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("AuthController IT — POST /api/v1/auth/clients")
class RegisterClientIT extends BaseWebIntegrationTest {


    // ─── 400: validation ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("400 — Validation errors")
    class ValidationTests {

        @Test
        @DisplayName("should return 400 when email is missing")
        void registerClient_shouldReturn400_whenEmailMissing() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Juan Pérez",
                    "vendorUuid", UUID.randomUUID().toString()
            ));
            mockMvc.perform(post("/api/v1/auth/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void registerClient_shouldReturn400_whenNameMissing() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", "cliente@correo.com",
                    "vendorUuid", UUID.randomUUID().toString()
            ));
            mockMvc.perform(post("/api/v1/auth/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when vendorUuid is missing")
        void registerClient_shouldReturn400_whenVendorUuidMissing() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", "cliente@correo.com",
                    "name", "Juan Pérez"
            ));
            mockMvc.perform(post("/api/v1/auth/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void registerClient_shouldReturn400_whenEmailInvalid() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", "not-an-email",
                    "name", "Juan Pérez",
                    "vendorUuid", UUID.randomUUID().toString()
            ));
            mockMvc.perform(post("/api/v1/auth/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── 201: happy path (public, no token needed) ───────────────────────────

    @Nested
    @DisplayName("201 — Successful registration (public)")
    class SuccessTests {

        @Test
        @DisplayName("should return 201 without any JWT token (public endpoint)")
        void registerClient_shouldReturn201_withoutToken() throws Exception {
            RegisterClientResult mockResult = new RegisterClientResult(
                    "auth-uid-123",
                    UUID.randomUUID(),
                    "Juan Pérez",
                    "cliente@correo.com"
            );

            when(registerClientUseCase.register(any())).thenReturn(mockResult);

            UUID vendorUuid = UUID.randomUUID();
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", "cliente@correo.com",
                    "password", "secret123!",
                    "name", "Juan Pérez",
                    "phone", "+502 5555-1234",
                    "vendorUuid", vendorUuid.toString()
            ));

            mockMvc.perform(post("/api/v1/auth/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.clientUuid").isNotEmpty())
                    .andExpect(jsonPath("$.externalId").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("Juan Pérez"))
                    .andExpect(jsonPath("$.email").value("cliente@correo.com"));

        }
    }
}
