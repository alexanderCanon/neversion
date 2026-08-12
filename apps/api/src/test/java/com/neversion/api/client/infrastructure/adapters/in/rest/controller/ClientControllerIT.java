package com.neversion.api.client.infrastructure.adapters.in.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.client.application.port.in.ClientUseCase;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientAccessDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientOrderHistoryDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientOrderServiceDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientReservationStatusDetail;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

/**
 * Integration tests for ClientController — EPIC-04 (US-029..032).
 *
 * Strategy: @MockBean on ClientUseCase + SubscriptionRepositoryPort
 * to isolate the HTTP + Security layer from DB.
 * JWT built with Nimbus (on classpath via oauth2-resource-server).
 */
import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("ClientController IT — EPIC-04")
class ClientControllerIT extends BaseWebIntegrationTest {


    // Must match application-test.yaml supabase.jwt.secret
    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final UUID CLIENT_UUID = UUID.randomUUID();

    /** Builds a signed HS256 JWT matching SupabaseJwtAuthConverter claim extraction. */
    private String buildJwt(String role) throws Exception {
        JWSSigner signer = new MACSigner(JWT_SECRET.getBytes());
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

    private Client buildClient() {
        return Client.builder()
                .id(1L)
                .uuid(CLIENT_UUID)
                .vendorId(10L)
                .name("Juan Pérez")
                .email("juan@test.com")
                .phone("55551234")
                .notes("Test")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── US-029: GET /clients/vendor/{vendorUuid} ──────────────────────────

    @Nested
    @DisplayName("US-029 — GET /api/v1/clients/vendor/{vendorUuid}")
    class ListByVendor {

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void listByVendor_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/v1/clients/vendor/{uuid}", VENDOR_UUID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 when caller has CLIENT role")
        void listByVendor_clientRole_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/clients/vendor/{uuid}", VENDOR_UUID)
                            .header("Authorization", "Bearer " + buildJwt("client")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 200 with client list when VENDOR role")
        void listByVendor_vendorRole_shouldReturn200() throws Exception {
            when(clientUseCase.listByVendor(eq(VENDOR_UUID), isNull(), isNull(), isNull(),
                    anyString()))
                    .thenReturn(List.of(buildClient()));
            when(subscriptionRepositoryPort.findByClientId(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/clients/vendor/{uuid}", VENDOR_UUID)
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").isNotEmpty())
                    .andExpect(jsonPath("$[0].name").value("Juan Pérez"))
                    .andExpect(jsonPath("$[0].activeSubscriptionCount").value(0));
        }

        @Test
        @DisplayName("should pass name filter to use case")
        void listByVendor_withNameFilter_shouldPassToUseCase() throws Exception {
            when(clientUseCase.listByVendor(eq(VENDOR_UUID), eq("Juan"), isNull(), isNull(),
                    anyString()))
                    .thenReturn(List.of(buildClient()));
            when(subscriptionRepositoryPort.findByClientId(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/clients/vendor/{uuid}", VENDOR_UUID)
                            .param("name", "Juan")
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Juan Pérez"));
        }
    }

    // ── US-030: GET /clients/{id}/detail ─────────────────────────────────

    @Nested
    @DisplayName("US-030 — GET /api/v1/clients/{id}/detail")
    class GetDetail {

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void getDetail_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/v1/clients/{id}/detail", CLIENT_UUID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 200 with client detail when VENDOR role")
        void getDetail_vendorRole_shouldReturn200() throws Exception {
            ClientDetail detail = new ClientDetail(buildClient(), List.of(), List.of());
            when(clientUseCase.getDetail(eq(CLIENT_UUID), anyString())).thenReturn(detail);

            mockMvc.perform(get("/api/v1/clients/{id}/detail", CLIENT_UUID)
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.client.name").value("Juan Pérez"))
                    .andExpect(jsonPath("$.activeSubscriptions").isArray())
                    .andExpect(jsonPath("$.orderHistory").isArray());
        }

        @Test
        @DisplayName("should return 403 when CLIENT role")
        void getDetail_clientRole_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/clients/{id}/detail", CLIENT_UUID)
                            .header("Authorization", "Bearer " + buildJwt("client")))
                    .andExpect(status().isForbidden());
        }
    }

    // ── EPIC-09 / US-061: GET /clients/me/accesses ───────────────────────

    @Nested
    @DisplayName("EPIC-09 / US-061 — GET /api/v1/clients/me/accesses")
    class GetMyAccesses {

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void getMyAccesses_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me/accesses"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 200 when CLIENT role")
        void getMyAccesses_clientRole_shouldReturn200() throws Exception {
            UUID subscriptionId = UUID.randomUUID();
            when(clientUseCase.getMyAccesses(anyString()))
                    .thenReturn(List.of(new ClientAccessDetail(
                            subscriptionId,
                            "Netflix",
                            "account@test.com",
                            "secret",
                            "Perfil 1",
                            "1234",
                            java.time.LocalDate.now().plusDays(30),
                            "ACTIVE")));

            mockMvc.perform(get("/api/v1/clients/me/accesses")
                            .header("Authorization", "Bearer " + buildJwt("client")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].subscriptionId").value(subscriptionId.toString()))
                    .andExpect(jsonPath("$[0].serviceName").value("Netflix"));
        }

        @Test
        @DisplayName("should return 403 when VENDOR role")
        void getMyAccesses_vendorRole_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me/accesses")
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isForbidden());
        }
    }

    // ── EPIC-09 / US-059: GET /clients/me/orders ────────────────────────

    @Nested
    @DisplayName("EPIC-09 / US-059 — GET /api/v1/clients/me/orders")
    class GetMyOrders {

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void getMyOrders_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me/orders"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 200 when CLIENT role")
        void getMyOrders_clientRole_shouldReturn200() throws Exception {
            UUID orderId = UUID.randomUUID();
            UUID serviceId = UUID.randomUUID();
            when(clientUseCase.getMyOrders(anyString()))
                    .thenReturn(List.of(new ClientOrderHistoryDetail(
                            orderId,
                            UUID.randomUUID(),
                            "COMPLETED",
                            "TRANSFERENCIA",
                            java.math.BigDecimal.valueOf(100),
                            java.math.BigDecimal.ZERO,
                            "https://receipt.test/file.png",
                            java.time.Instant.parse("2026-04-30T10:00:00Z"),
                            java.time.Instant.parse("2026-04-30T09:00:00Z"),
                            List.of(new ClientOrderServiceDetail(serviceId, "Netflix", 1)))));

            mockMvc.perform(get("/api/v1/clients/me/orders")
                            .header("Authorization", "Bearer " + buildJwt("client")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(orderId.toString()))
                    .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                    .andExpect(jsonPath("$[0].total").value(100))
                    .andExpect(jsonPath("$[0].services[0].serviceId").value(serviceId.toString()))
                    .andExpect(jsonPath("$[0].services[0].serviceName").value("Netflix"));
        }

        @Test
        @DisplayName("should return 403 when VENDOR role")
        void getMyOrders_vendorRole_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me/orders")
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isForbidden());
        }
    }

    // ── EPIC-09 / US-060: GET /clients/me/reservations ───────────────────

    @Nested
    @DisplayName("EPIC-09 / US-060 — GET /api/v1/clients/me/reservations")
    class GetMyReservations {

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void getMyReservations_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me/reservations"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 200 when CLIENT role")
        void getMyReservations_clientRole_shouldReturn200() throws Exception {
            UUID reservationId = UUID.randomUUID();
            UUID serviceId = UUID.randomUUID();
            when(clientUseCase.getMyReservations(anyString()))
                    .thenReturn(List.of(new ClientReservationStatusDetail(
                            reservationId,
                            "REJECTED",
                            java.math.BigDecimal.valueOf(100),
                            java.math.BigDecimal.ZERO,
                            "https://receipt.test/file.png",
                            "TRANSFERENCIA",
                            java.time.Instant.parse("2026-04-30T10:00:00Z"),
                            java.time.Instant.parse("2026-04-30T09:00:00Z"),
                            "Comprobante ilegible",
                            null,
                            List.of(new ClientOrderServiceDetail(serviceId, "Netflix", 1)))));

            mockMvc.perform(get("/api/v1/clients/me/reservations")
                            .header("Authorization", "Bearer " + buildJwt("client")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(reservationId.toString()))
                    .andExpect(jsonPath("$[0].status").value("REJECTED"))
                    .andExpect(jsonPath("$[0].notes").value("Comprobante ilegible"))
                    .andExpect(jsonPath("$[0].services[0].serviceName").value("Netflix"));
        }

        @Test
        @DisplayName("should return 403 when VENDOR role")
        void getMyReservations_vendorRole_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me/reservations")
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isForbidden());
        }
    }

    // ── EPIC-09 / US-062: GET/PUT /clients/me ────────────────────────────

    @Nested
    @DisplayName("EPIC-09 / US-062 — GET/PUT /api/v1/clients/me")
    class MyProfile {

        @Test
        @DisplayName("GET should return 401 when no JWT provided")
        void getMyProfile_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET should return 200 when CLIENT role")
        void getMyProfile_clientRole_shouldReturn200() throws Exception {
            when(clientUseCase.getMyProfile(anyString())).thenReturn(buildClient());

            mockMvc.perform(get("/api/v1/clients/me")
                            .header("Authorization", "Bearer " + buildJwt("client")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(CLIENT_UUID.toString()))
                    .andExpect(jsonPath("$.name").value("Juan Pérez"))
                    .andExpect(jsonPath("$.email").value("juan@test.com"));
        }

        @Test
        @DisplayName("GET should return 403 when VENDOR role")
        void getMyProfile_vendorRole_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/v1/clients/me")
                            .header("Authorization", "Bearer " + buildJwt("vendor")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT should return 401 when no JWT provided")
        void updateMyProfile_noToken_shouldReturn401() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Juan Actualizado",
                    "phone", "59998888"
            ));

            mockMvc.perform(put("/api/v1/clients/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT should return 403 when VENDOR role")
        void updateMyProfile_vendorRole_shouldReturn403() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Juan Actualizado",
                    "phone", "59998888"
            ));

            mockMvc.perform(put("/api/v1/clients/me")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT should return 400 when name is missing")
        void updateMyProfile_missingName_shouldReturn400() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("phone", "59998888"));

            mockMvc.perform(put("/api/v1/clients/me")
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT should return 400 when phone is invalid")
        void updateMyProfile_invalidPhone_shouldReturn400() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Juan Actualizado",
                    "phone", "999988888"
            ));

            mockMvc.perform(put("/api/v1/clients/me")
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT should update name and phone when CLIENT role")
        void updateMyProfile_clientRole_shouldReturn200() throws Exception {
            Client updated = Client.builder()
                    .id(1L).uuid(CLIENT_UUID).vendorId(10L)
                    .name("Juan Actualizado")
                    .email("juan@test.com")
                    .phone("50259998888")
                    .notes("Test")
                    .createdAt(LocalDateTime.now())
                    .build();
            when(clientUseCase.updateMyProfile(eq("Juan Actualizado"), eq("59998888"), anyString()))
                    .thenReturn(updated);

            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Juan Actualizado",
                    "phone", "59998888"
            ));

            mockMvc.perform(put("/api/v1/clients/me")
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Juan Actualizado"))
                    .andExpect(jsonPath("$.phone").value("50259998888"))
                    .andExpect(jsonPath("$.email").value("juan@test.com"));
        }
    }

    // ── US-031: POST /clients ─────────────────────────────────────────────


    @Nested
    @DisplayName("US-031 — POST /api/v1/clients")
    class CreateClient {

        private String validBody() throws Exception {
            return objectMapper.writeValueAsString(Map.of(
                    "name", "Ana López",
                    "email", "ana@test.com",
                    "phone", "55551111"
            ));
        }

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void create_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(post("/api/v1/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 when CLIENT role")
        void create_clientRole_shouldReturn403() throws Exception {
            mockMvc.perform(post("/api/v1/clients")
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 400 when phone is missing")
        void create_missingPhone_shouldReturn400() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("name", "Ana López"));
            mockMvc.perform(post("/api/v1/clients")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void create_invalidEmail_shouldReturn400() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Ana López",
                    "email", "not-an-email",
                    "phone", "55551111"
            ));
            mockMvc.perform(post("/api/v1/clients")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when phone format is invalid")
        void create_invalidPhone_shouldReturn400() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Ana López",
                    "phone", "88888888"
            ));
            mockMvc.perform(post("/api/v1/clients")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 201 when VENDOR creates client successfully")
        void create_validRequest_shouldReturn201() throws Exception {
            Client saved = buildClient();
            when(clientUseCase.createForVendor(any(Client.class), anyString())).thenReturn(saved);

            mockMvc.perform(post("/api/v1/clients")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("Juan Pérez"))
                    .andExpect(jsonPath("$.email").value("juan@test.com"));
        }

        @Test
        @DisplayName("should return 400 when service throws IllegalArgumentException (dup email)")
        void create_duplicateEmail_shouldReturn400() throws Exception {
            when(clientUseCase.createForVendor(any(Client.class), anyString()))
                    .thenThrow(new IllegalArgumentException("Email already registered: ana@test.com"));

            mockMvc.perform(post("/api/v1/clients")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── US-032: PUT /clients/{id} ─────────────────────────────────────────

    @Nested
    @DisplayName("US-032 — PUT /api/v1/clients/{id}")
    class UpdateClient {

        private String validBody() throws Exception {
            return objectMapper.writeValueAsString(Map.of(
                    "name", "Juan Actualizado",
                    "phone", "59998888",
                    "notes", "VIP"
            ));
        }

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void update_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(put("/api/v1/clients/{id}", CLIENT_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 when CLIENT role")
        void update_clientRole_shouldReturn403() throws Exception {
            mockMvc.perform(put("/api/v1/clients/{id}", CLIENT_UUID)
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void update_missingName_shouldReturn400() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("phone", "55551234"));
            mockMvc.perform(put("/api/v1/clients/{id}", CLIENT_UUID)
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when phone is invalid")
        void update_invalidPhone_shouldReturn400() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Juan Actualizado",
                    "phone", "12345678",
                    "notes", "VIP"
            ));
            mockMvc.perform(put("/api/v1/clients/{id}", CLIENT_UUID)
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 200 when VENDOR updates own client")
        void update_ownedClient_shouldReturn200() throws Exception {
            Client updated = Client.builder()
                    .id(1L).uuid(CLIENT_UUID).vendorId(10L)
                    .name("Juan Actualizado").phone("50259998888").notes("VIP")
                    .email("juan@test.com").createdAt(LocalDateTime.now())
                    .build();
            when(clientUseCase.update(eq(CLIENT_UUID), eq("Juan Actualizado"),
                    eq("59998888"), eq("VIP"), anyString())).thenReturn(updated);

            mockMvc.perform(put("/api/v1/clients/{id}", CLIENT_UUID)
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Juan Actualizado"))
                    .andExpect(jsonPath("$.phone").value("50259998888"));
        }
    }
}
