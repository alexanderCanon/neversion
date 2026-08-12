package com.neversion.api.subscription.infrastructure.adapters.in.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.DetectExpiredSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase.SubscriptionDetail;
import com.neversion.api.subscription.application.port.in.ListSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.RenewSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.RevokeSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.UpdateSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("SubscriptionController Web Integration Tests")
class SubscriptionControllerIT extends BaseWebIntegrationTest {


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
    @DisplayName("POST /api/v1/subscriptions")
    class AssignTests {

        @Test
        @DisplayName("should create manual subscription successfully")
        void assign_success() throws Exception {
            String callerSubject = "auth|vendor-user";
            UUID subscriptionUuid = UUID.randomUUID();
            UUID clientUuid = UUID.randomUUID();
            UUID profileUuid = UUID.randomUUID();
            UUID serviceUuid = UUID.randomUUID();
            
            Subscription createdSub = Subscription.builder()
                    .uuid(subscriptionUuid)
                    .profileId(5L)
                    .clientId(10L)
                    .status(SubStatus.ACTIVE)
                    .build();

            when(createManualSubscriptionUseCase.create(any(Subscription.class), eq(true), eq(callerSubject)))
                    .thenReturn(createdSub);

            String requestBody = """
                    {
                      "clientId": "%s",
                      "profileId": "%s",
                      "serviceId": "%s",
                      "priceSold": 75.00,
                      "discountApplied": 0.00,
                      "paymentDueDate": "2026-05-01",
                      "notes": "Manual notes",
                      "sendNotification": true
                    }
                    """.formatted(clientUuid, profileUuid, serviceUuid);

            mockMvc.perform(post("/api/v1/subscriptions")
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(subscriptionUuid.toString()))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/subscriptions/{id}")
    class GetSubscriptionDetailTests {

        @Test
        @DisplayName("should get subscription detail successfully")
        void getById_success() throws Exception {
            UUID subscriptionUuid = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            
            SubscriptionDetail detail = new SubscriptionDetail(
                    Subscription.builder()
                            .uuid(subscriptionUuid)
                            .status(SubStatus.ACTIVE)
                            .startDate(LocalDate.of(2026, 4, 1))
                            .endDate(LocalDate.of(2026, 5, 1))
                            .paymentDueDate(LocalDate.of(2026, 5, 1))
                            .build(),
                    Client.builder()
                            .id(10L)
                            .name("Client Name")
                            .build(),
                    Profile.builder()
                            .id(5L)
                            .name("Casa 1")
                            .pin("1234")
                            .build(),
                    Account.builder()
                            .id(20L)
                            .email("netflix-email@test.com")
                            .password("secret")
                            .plan("Premium")
                            .build(),
                    Service.builder()
                            .id(1L)
                            .name("Netflix")
                            .category(CategoryType.STREAMING)
                            .build(),
                    Order.builder()
                            .uuid(UUID.randomUUID())
                            .build()
            );

            when(getSubscriptionDetailUseCase.getDetail(eq(subscriptionUuid), eq(callerSubject)))
                    .thenReturn(detail);

            mockMvc.perform(get("/api/v1/subscriptions/" + subscriptionUuid)
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(subscriptionUuid.toString()))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.client.name").value("Client Name"))
                    .andExpect(jsonPath("$.access.accountEmail").value("netflix-email@test.com"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/subscriptions/{id}/suspend")
    class SuspendTests {

        @Test
        @DisplayName("should suspend subscription successfully")
        void suspend_success() throws Exception {
            UUID subscriptionUuid = UUID.randomUUID();
            
            Subscription suspended = Subscription.builder()
                    .uuid(subscriptionUuid)
                    .status(SubStatus.SUSPENDED)
                    .build();

            when(updateSubscriptionUseCase.suspend(eq(subscriptionUuid)))
                    .thenReturn(suspended);

            mockMvc.perform(put("/api/v1/subscriptions/" + subscriptionUuid + "/suspend")
                            .header("Authorization", "Bearer " + buildJwt("vendor", "auth|vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUSPENDED"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/subscriptions/{id}/renew")
    class RenewTests {

        @Test
        @DisplayName("should renew subscription successfully")
        void renew_success() throws Exception {
            UUID subscriptionUuid = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            
            Subscription renewed = Subscription.builder()
                    .uuid(subscriptionUuid)
                    .status(SubStatus.ACTIVE)
                    .build();

            when(renewSubscriptionUseCase.renew(eq(subscriptionUuid), eq(callerSubject)))
                    .thenReturn(renewed);

            mockMvc.perform(put("/api/v1/subscriptions/" + subscriptionUuid + "/renew")
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/subscriptions/{id}/cancel")
    class CancelTests {

        @Test
        @DisplayName("should revoke subscription successfully")
        void cancel_success() throws Exception {
            UUID subscriptionUuid = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            
            Subscription cancelled = Subscription.builder()
                    .uuid(subscriptionUuid)
                    .status(SubStatus.CANCELLED)
                    .build();

            when(revokeSubscriptionUseCase.revoke(eq(subscriptionUuid), eq(callerSubject)))
                    .thenReturn(cancelled);

            mockMvc.perform(put("/api/v1/subscriptions/" + subscriptionUuid + "/cancel")
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/subscriptions/detect-expired")
    class DetectExpiredTests {

        @Test
        @DisplayName("should trigger detect-expired successfully")
        void detectExpired_success() throws Exception {
            when(detectExpiredSubscriptionsUseCase.detectAndSuspend()).thenReturn(5);

            mockMvc.perform(post("/api/v1/subscriptions/detect-expired")
                            .header("Authorization", "Bearer " + buildJwt("vendor", "auth|vendor")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.suspendedCount").value(5));
        }
    }
}
