package com.neversion.api.reservation.infrastructure.adapters.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.reservation.application.port.in.CreateRenewalReservationUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("ReservationController IT — EPIC-09 / US-061")
class ReservationControllerIT extends BaseWebIntegrationTest {


    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private static final UUID SUBSCRIPTION_UUID = UUID.randomUUID();

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

    private String validBody() {
        return """
                {
                  "subscriptionId": "%s",
                  "paymentMethod": "TRANSFERENCIA"
                }
                """.formatted(SUBSCRIPTION_UUID);
    }

    @Nested
    @DisplayName("POST /api/v1/reservations/renew")
    class CreateRenewalReservation {

        @Test
        @DisplayName("should return 401 when no JWT provided")
        void createRenewal_noToken_shouldReturn401() throws Exception {
            mockMvc.perform(post("/api/v1/reservations/renew")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 403 when caller has VENDOR role")
        void createRenewal_vendorRole_shouldReturn403() throws Exception {
            mockMvc.perform(post("/api/v1/reservations/renew")
                            .header("Authorization", "Bearer " + buildJwt("vendor"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 201 when caller has CLIENT role")
        void createRenewal_clientRole_shouldReturn201() throws Exception {
            UUID reservationUuid = UUID.randomUUID();
            when(createRenewalReservationUseCase.create(eq(SUBSCRIPTION_UUID), eq("TRANSFERENCIA"), anyString()))
                    .thenReturn(Reservation.builder()
                            .id(1L)
                            .uuid(reservationUuid)
                            .clientUuid(UUID.randomUUID())
                            .vendorId(10L)
                            .status(ReservationStatus.PENDING)
                            .discount(BigDecimal.ZERO)
                            .total(new BigDecimal("75.00"))
                            .paymentMethod("TRANSFERENCIA")
                            .expirationDate(Instant.now().plusSeconds(3600))
                            .createdAt(Instant.now())
                            .renewalSubscriptionUuid(SUBSCRIPTION_UUID)
                            .build());

            mockMvc.perform(post("/api/v1/reservations/renew")
                            .header("Authorization", "Bearer " + buildJwt("client"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(reservationUuid.toString()))
                    .andExpect(jsonPath("$.renewalSubscriptionId").value(SUBSCRIPTION_UUID.toString()));
        }
    }
}
