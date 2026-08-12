package com.neversion.api.assignment.infrastructure.adapters.in.rest.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.assignment.application.port.in.ConfirmAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.ManualAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.SuggestAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.dto.AssignmentResult;
import com.neversion.api.assignment.application.port.in.dto.AssignmentSuggestion;

import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("AssignmentController Web Integration Tests")
class AssignmentControllerIT extends BaseWebIntegrationTest {


    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private String buildJwt(String role, String subject) throws Exception {
        JWSSigner signer = new MACSigner(JWT_SECRET.getBytes());
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                .claim("app_metadata", Map.of("role", role))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    @Nested
    @DisplayName("GET /api/v1/assignments/suggest/{orderId}")
    class SuggestTests {

        @Test
        @DisplayName("should return assignment suggestion successfully")
        void suggest_success() throws Exception {
            UUID orderId = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            UUID profileUuid = UUID.randomUUID();
            UUID accountUuid = UUID.randomUUID();

            AssignmentSuggestion suggestion = new AssignmentSuggestion(
                    true,
                    SaleMode.BY_PROFILE,
                    profileUuid,
                    accountUuid,
                    "Netflix",
                    "netflix@example.com",
                    null
            );

            when(suggestAssignmentUseCase.suggest(eq(orderId), eq(callerSubject)))
                    .thenReturn(suggestion);

            mockMvc.perform(get("/api/v1/assignments/suggest/" + orderId)
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasSuggestion").value(true))
                    .andExpect(jsonPath("$.saleMode").value("BY_PROFILE"))
                    .andExpect(jsonPath("$.suggestedProfileId").value(profileUuid.toString()))
                    .andExpect(jsonPath("$.suggestedAccountId").value(accountUuid.toString()))
                    .andExpect(jsonPath("$.serviceName").value("Netflix"))
                    .andExpect(jsonPath("$.accountEmail").value("netflix@example.com"));
        }

        @Test
        @DisplayName("should return 401 when no token is provided")
        void suggest_unauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/assignments/suggest/" + UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/assignments/confirm/{orderId}")
    class ConfirmTests {

        @Test
        @DisplayName("should confirm assignment successfully")
        void confirm_success() throws Exception {
            UUID orderId = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            UUID subscriptionUuid = UUID.randomUUID();
            UUID profileUuid = UUID.randomUUID();
            UUID clientUuid = UUID.randomUUID();

            AssignmentResult result = new AssignmentResult(
                    subscriptionUuid,
                    orderId,
                    profileUuid,
                    clientUuid,
                    "Netflix",
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 5, 1),
                    true
            );

            when(confirmAssignmentUseCase.confirm(eq(orderId), eq(profileUuid), eq(callerSubject)))
                    .thenReturn(result);

            mockMvc.perform(post("/api/v1/assignments/confirm/" + orderId)
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"profileId\":\"" + profileUuid + "\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.subscriptionId").value(subscriptionUuid.toString()));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void confirm_badRequest() throws Exception {
            mockMvc.perform(post("/api/v1/assignments/confirm/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + buildJwt("vendor", "auth|user"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")) // profileId is missing and required
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/assignments/manual")
    class ManualAssignmentTests {

        @Test
        @DisplayName("should create manual assignment successfully")
        void manual_success() throws Exception {
            String callerSubject = "auth|vendor-user";
            UUID subscriptionUuid = UUID.randomUUID();
            UUID clientUuid = UUID.randomUUID();
            UUID serviceUuid = UUID.randomUUID();
            UUID profileUuid = UUID.randomUUID();

            AssignmentResult result = new AssignmentResult(
                    subscriptionUuid,
                    null,
                    profileUuid,
                    clientUuid,
                    "Netflix",
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 5, 1),
                    true
            );

            when(manualAssignmentUseCase.assign(eq(clientUuid), eq(serviceUuid), eq(profileUuid), eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 5, 1)), eq(callerSubject)))
                    .thenReturn(result);

            String requestBody = """
                    {
                      "clientId": "%s",
                      "serviceId": "%s",
                      "profileId": "%s",
                      "startDate": "2026-04-01",
                      "endDate": "2026-05-01"
                    }
                    """.formatted(clientUuid, serviceUuid, profileUuid);

            mockMvc.perform(post("/api/v1/assignments/manual")
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.subscriptionId").value(subscriptionUuid.toString()));
        }
    }
}
