package com.neversion.api.dashboard.infrastructure.adapters.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.dashboard.application.port.in.GetActiveClientsKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetAccountsByProductUseCase;
import com.neversion.api.dashboard.application.port.in.GetExpiringSubscriptionsKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetGrossProfitKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetInventoryAvailabilityKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetProductsSummaryUseCase;
import com.neversion.api.dashboard.application.port.in.GetProfilesByAccountUseCase;
import com.neversion.api.dashboard.application.port.in.GetSuccessfulRenewalsKpiUseCase;
import com.neversion.api.dashboard.application.result.ActiveClientsKpiResult;
import com.neversion.api.dashboard.application.result.ExpiringSubscriptionResult;
import com.neversion.api.dashboard.application.result.ExpiringSubscriptionsKpiResult;
import com.neversion.api.dashboard.application.result.GrossProfitKpiResult;
import com.neversion.api.dashboard.application.result.InventoryAvailabilityResult;
import com.neversion.api.dashboard.application.result.SuccessfulRenewalsKpiResult;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DashboardController IT — EPIC-10 KPIs")
class DashboardControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetProductsSummaryUseCase getProductsSummaryUseCase;

    @MockBean
    private GetAccountsByProductUseCase getAccountsByProductUseCase;

    @MockBean
    private GetProfilesByAccountUseCase getProfilesByAccountUseCase;

    @MockBean
    private GetExpiringSubscriptionsKpiUseCase getExpiringSubscriptionsKpiUseCase;

    @MockBean
    private GetInventoryAvailabilityKpiUseCase getInventoryAvailabilityKpiUseCase;

    @MockBean
    private GetActiveClientsKpiUseCase getActiveClientsKpiUseCase;

    @MockBean
    private GetSuccessfulRenewalsKpiUseCase getSuccessfulRenewalsKpiUseCase;

    @MockBean
    private GetGrossProfitKpiUseCase getGrossProfitKpiUseCase;

    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    @Test
    @DisplayName("GET /dashboard/kpis/expiring-subscriptions - should return 401 without token")
    void getExpiringSubscriptions_noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/expiring-subscriptions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/expiring-subscriptions - should return 403 for super admin")
    void getExpiringSubscriptions_superAdminRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/expiring-subscriptions")
                        .header("Authorization", "Bearer " + buildJwt("super_admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/expiring-subscriptions - should return grouped KPI for vendor")
    void getExpiringSubscriptions_vendorRole_shouldReturn200() throws Exception {
        UUID subscriptionId = UUID.randomUUID();
        when(getExpiringSubscriptionsKpiUseCase.getForAuthenticatedVendor(anyString()))
                .thenReturn(new ExpiringSubscriptionsKpiResult(
                        List.of(new ExpiringSubscriptionResult(
                                subscriptionId,
                                "Cliente Uno",
                                "Netflix",
                                "Perfil 1",
                                LocalDate.of(2026, 4, 30),
                                "ACTIVE")),
                        List.of(),
                        List.of()));

        mockMvc.perform(get("/api/v1/dashboard/kpis/expiring-subscriptions")
                        .header("Authorization", "Bearer " + buildJwt("vendor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.today[0].subscriptionId").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.today[0].clientName").value("Cliente Uno"))
                .andExpect(jsonPath("$.today[0].serviceName").value("Netflix"))
                .andExpect(jsonPath("$.tomorrow").isArray())
                .andExpect(jsonPath("$.thisWeek").isArray());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/inventory-availability - should return 401 without token")
    void getInventoryAvailability_noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/inventory-availability"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/inventory-availability - should return 403 for super admin")
    void getInventoryAvailability_superAdminRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/inventory-availability")
                        .header("Authorization", "Bearer " + buildJwt("super_admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/inventory-availability - should return grouped inventory for vendor")
    void getInventoryAvailability_vendorRole_shouldReturn200() throws Exception {
        UUID serviceId = UUID.randomUUID();
        when(getInventoryAvailabilityKpiUseCase.getForAuthenticatedVendor(anyString()))
                .thenReturn(List.of(new InventoryAvailabilityResult(
                        serviceId,
                        "Netflix",
                        3,
                        2,
                        1,
                        4)));

        mockMvc.perform(get("/api/v1/dashboard/kpis/inventory-availability")
                        .header("Authorization", "Bearer " + buildJwt("vendor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceId").value(serviceId.toString()))
                .andExpect(jsonPath("$[0].serviceName").value("Netflix"))
                .andExpect(jsonPath("$[0].availableProfiles").value(3))
                .andExpect(jsonPath("$[0].occupiedProfiles").value(2))
                .andExpect(jsonPath("$[0].availableFullAccounts").value(1))
                .andExpect(jsonPath("$[0].occupiedFullAccounts").value(4));
    }

    @Test
    @DisplayName("GET /dashboard/kpis/active-clients - should return 401 without token")
    void getActiveClients_noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/active-clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/active-clients - should return 403 for super admin")
    void getActiveClients_superAdminRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/active-clients")
                        .header("Authorization", "Bearer " + buildJwt("super_admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/active-clients - should return count for vendor")
    void getActiveClients_vendorRole_shouldReturn200() throws Exception {
        when(getActiveClientsKpiUseCase.getForAuthenticatedVendor(anyString()))
                .thenReturn(new ActiveClientsKpiResult(7L));

        mockMvc.perform(get("/api/v1/dashboard/kpis/active-clients")
                        .header("Authorization", "Bearer " + buildJwt("vendor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeClientsCount").value(7));
    }

    @Test
    @DisplayName("GET /dashboard/kpis/successful-renewals - should return 401 without token")
    void getSuccessfulRenewals_noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/successful-renewals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/successful-renewals - should return 403 for super admin")
    void getSuccessfulRenewals_superAdminRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/successful-renewals")
                        .header("Authorization", "Bearer " + buildJwt("super_admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/successful-renewals - should return count for vendor")
    void getSuccessfulRenewals_vendorRole_shouldReturn200() throws Exception {
        when(getSuccessfulRenewalsKpiUseCase.getForAuthenticatedVendor(anyString()))
                .thenReturn(new SuccessfulRenewalsKpiResult(5L));

        mockMvc.perform(get("/api/v1/dashboard/kpis/successful-renewals")
                        .header("Authorization", "Bearer " + buildJwt("vendor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulRenewalsCount").value(5));
    }

    @Test
    @DisplayName("GET /dashboard/kpis/gross-profit - should return 401 without token")
    void getGrossProfit_noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/gross-profit"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/gross-profit - should return 403 for super admin")
    void getGrossProfit_superAdminRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/kpis/gross-profit")
                        .header("Authorization", "Bearer " + buildJwt("super_admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /dashboard/kpis/gross-profit - should return current month GTQ amount for vendor")
    void getGrossProfit_vendorRole_shouldReturn200() throws Exception {
        when(getGrossProfitKpiUseCase.getForAuthenticatedVendor(anyString()))
                .thenReturn(new GrossProfitKpiResult(new BigDecimal("245.50"), "GTQ"));

        mockMvc.perform(get("/api/v1/dashboard/kpis/gross-profit")
                        .header("Authorization", "Bearer " + buildJwt("vendor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grossProfit").value(245.50))
                .andExpect(jsonPath("$.currency").value("GTQ"));
    }

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
}
