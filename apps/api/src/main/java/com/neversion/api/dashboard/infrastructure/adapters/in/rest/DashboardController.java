package com.neversion.api.dashboard.infrastructure.adapters.in.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.dashboard.application.port.in.GetAccountProfitMarginsUseCase;
import com.neversion.api.dashboard.application.port.in.GetActiveClientsKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetAccountsByProductUseCase;
import com.neversion.api.dashboard.application.port.in.GetExpiringAccountsKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetExpiringSubscriptionsKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetGrossProfitKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetInventoryAvailabilityKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetProductsSummaryUseCase;
import com.neversion.api.dashboard.application.port.in.GetProfilesByAccountUseCase;
import com.neversion.api.dashboard.application.port.in.GetSuccessfulRenewalsKpiUseCase;
import com.neversion.api.dashboard.application.result.ActiveClientsKpiResult;
import com.neversion.api.dashboard.application.result.AccountGroupResult;
import com.neversion.api.dashboard.application.result.ExpiringAccountsKpiResult;
import com.neversion.api.dashboard.application.result.ExpiringSubscriptionsKpiResult;
import com.neversion.api.dashboard.application.result.GrossProfitKpiResult;
import com.neversion.api.dashboard.application.result.InventoryAvailabilityResult;
import com.neversion.api.dashboard.application.result.ProfitMarginsResult;
import com.neversion.api.dashboard.application.result.ProfileResult;
import com.neversion.api.dashboard.application.result.ProductSummaryResult;
import com.neversion.api.dashboard.application.result.SuccessfulRenewalsKpiResult;
import com.neversion.api.shared.domain.model.enums.CategoryType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/v1/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Dashboard", description = "Master dashboard for admin monitoring")
public class DashboardController {

    private final GetProductsSummaryUseCase getProductsSummaryUseCase;
    private final GetAccountsByProductUseCase getAccountsByProductUseCase;
    private final GetProfilesByAccountUseCase getProfilesByAccountUseCase;
    private final GetExpiringSubscriptionsKpiUseCase getExpiringSubscriptionsKpiUseCase;
    private final GetExpiringAccountsKpiUseCase getExpiringAccountsKpiUseCase;
    private final GetInventoryAvailabilityKpiUseCase getInventoryAvailabilityKpiUseCase;
    private final GetActiveClientsKpiUseCase getActiveClientsKpiUseCase;
    private final GetSuccessfulRenewalsKpiUseCase getSuccessfulRenewalsKpiUseCase;
    private final GetGrossProfitKpiUseCase getGrossProfitKpiUseCase;
    private final GetAccountProfitMarginsUseCase getAccountProfitMarginsUseCase;

    public DashboardController(GetProductsSummaryUseCase getProductsSummaryUseCase,
            GetAccountsByProductUseCase getAccountsByProductUseCase,
            GetProfilesByAccountUseCase getProfilesByAccountUseCase,
            GetExpiringSubscriptionsKpiUseCase getExpiringSubscriptionsKpiUseCase,
            GetExpiringAccountsKpiUseCase getExpiringAccountsKpiUseCase,
            GetInventoryAvailabilityKpiUseCase getInventoryAvailabilityKpiUseCase,
            GetActiveClientsKpiUseCase getActiveClientsKpiUseCase,
            GetSuccessfulRenewalsKpiUseCase getSuccessfulRenewalsKpiUseCase,
            GetGrossProfitKpiUseCase getGrossProfitKpiUseCase,
            GetAccountProfitMarginsUseCase getAccountProfitMarginsUseCase) {
        this.getProductsSummaryUseCase = getProductsSummaryUseCase;
        this.getAccountsByProductUseCase = getAccountsByProductUseCase;
        this.getProfilesByAccountUseCase = getProfilesByAccountUseCase;
        this.getExpiringSubscriptionsKpiUseCase = getExpiringSubscriptionsKpiUseCase;
        this.getExpiringAccountsKpiUseCase = getExpiringAccountsKpiUseCase;
        this.getInventoryAvailabilityKpiUseCase = getInventoryAvailabilityKpiUseCase;
        this.getActiveClientsKpiUseCase = getActiveClientsKpiUseCase;
        this.getSuccessfulRenewalsKpiUseCase = getSuccessfulRenewalsKpiUseCase;
        this.getGrossProfitKpiUseCase = getGrossProfitKpiUseCase;
        this.getAccountProfitMarginsUseCase = getAccountProfitMarginsUseCase;
    }

    @GetMapping("/kpis/expiring-subscriptions")
    @Operation(summary = "Get expiring subscriptions KPI (US-063)",
            description = "Returns the authenticated vendor's active or suspended subscriptions grouped by "
                    + "today, tomorrow and later this week.")
    @ApiResponse(responseCode = "200", description = "Expiring subscriptions KPI retrieved")
    @ApiResponse(responseCode = "403", description = "Only vendors can access this KPI")
    public ResponseEntity<ExpiringSubscriptionsKpiResult> getExpiringSubscriptions(
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(getExpiringSubscriptionsKpiUseCase
                .getForAuthenticatedVendor(extractExternalId(token)));
    }

    @GetMapping("/kpis/expiring-accounts")
    @Operation(summary = "Get expiring accounts KPI",
            description = "Returns the authenticated vendor's master accounts due for renewal grouped by "
                    + "today, tomorrow and later this week.")
    @ApiResponse(responseCode = "200", description = "Expiring accounts KPI retrieved")
    @ApiResponse(responseCode = "403", description = "Only vendors can access this KPI")
    public ResponseEntity<ExpiringAccountsKpiResult> getExpiringAccounts(
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(getExpiringAccountsKpiUseCase
                .getForAuthenticatedVendor(extractExternalId(token)));
    }

    @GetMapping("/kpis/inventory-availability")
    @Operation(summary = "Get inventory availability KPI (US-064)",
            description = "Returns the authenticated vendor's available and occupied profiles/full accounts grouped by service.")
    @ApiResponse(responseCode = "200", description = "Inventory availability KPI retrieved")
    @ApiResponse(responseCode = "403", description = "Only vendors can access this KPI")
    public ResponseEntity<List<InventoryAvailabilityResult>> getInventoryAvailability(
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(getInventoryAvailabilityKpiUseCase
                .getForAuthenticatedVendor(extractExternalId(token)));
    }

    @GetMapping("/kpis/active-clients")
    @Operation(summary = "Get active clients KPI (US-065)",
            description = "Returns the authenticated vendor's unique clients with at least one active subscription.")
    @ApiResponse(responseCode = "200", description = "Active clients KPI retrieved")
    @ApiResponse(responseCode = "403", description = "Only vendors can access this KPI")
    public ResponseEntity<ActiveClientsKpiResult> getActiveClients(JwtAuthenticationToken token) {
        return ResponseEntity.ok(getActiveClientsKpiUseCase
                .getForAuthenticatedVendor(extractExternalId(token)));
    }

    @GetMapping("/kpis/successful-renewals")
    @Operation(summary = "Get successful renewals KPI (US-066)",
            description = "Returns the authenticated vendor's successful renewal count for the current month.")
    @ApiResponse(responseCode = "200", description = "Successful renewals KPI retrieved")
    @ApiResponse(responseCode = "403", description = "Only vendors can access this KPI")
    public ResponseEntity<SuccessfulRenewalsKpiResult> getSuccessfulRenewals(JwtAuthenticationToken token) {
        return ResponseEntity.ok(getSuccessfulRenewalsKpiUseCase
                .getForAuthenticatedVendor(extractExternalId(token)));
    }

    @GetMapping("/kpis/gross-profit")
    @Operation(summary = "Get gross profit KPI (US-067)",
            description = "Returns the authenticated vendor's gross profit for the current calendar month in GTQ.")
    @ApiResponse(responseCode = "200", description = "Gross profit KPI retrieved")
    @ApiResponse(responseCode = "403", description = "Only vendors can access this KPI")
    public ResponseEntity<GrossProfitKpiResult> getGrossProfit(JwtAuthenticationToken token) {
        return ResponseEntity.ok(getGrossProfitKpiUseCase
                .getForAuthenticatedVendor(extractExternalId(token)));
    }

    @GetMapping("/account-profit-margins")
    @Operation(summary = "Get per-account profit margins",
            description = "Returns per-account and per-service profit margins for the specified calendar month "
                    + "(defaults to current month). Includes all non-expired accounts, even those with zero sales.")
    @ApiResponse(responseCode = "200", description = "Profit margins retrieved")
    @ApiResponse(responseCode = "403", description = "Only vendors can access this endpoint")
    public ResponseEntity<ProfitMarginsResult> getAccountProfitMargins(
            JwtAuthenticationToken token,
            @Parameter(description = "Year (e.g. 2026). Defaults to current year.")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month (1-12). Defaults to current month.")
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(getAccountProfitMarginsUseCase
                .getForAuthenticatedVendor(extractExternalId(token), year, month));
    }

    @GetMapping
    @Operation(summary = "Get streaming products summary",
            description = "Returns products filtered by category with account count per product")
    @ApiResponse(responseCode = "200", description = "Product summaries retrieved")
    public ResponseEntity<List<ProductSummaryResult>> getProductsSummary(
            @Parameter(description = "Product category (STREAMING, SOFTWARE, etc.)")
            @RequestParam CategoryType category) {
        List<ProductSummaryResult> result = getProductsSummaryUseCase.getByCategory(category);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/products/{productId}/accounts")
    @Operation(summary = "Get accounts for a product",
            description = "Returns all accounts for a product with profile availability")
    @ApiResponse(responseCode = "200", description = "Account groups retrieved")
    public ResponseEntity<List<AccountGroupResult>> getAccountsByProduct(
            @Parameter(description = "Product UUID") @PathVariable UUID productId) {
        List<AccountGroupResult> result = getAccountsByProductUseCase.getByProductId(productId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/accounts/{accountId}/profiles")
    @Operation(summary = "Get profiles for an account",
            description = "Returns profiles with subscription and customer data for an account")
    @ApiResponse(responseCode = "200", description = "Account profiles retrieved")
    public ResponseEntity<List<ProfileResult>> getProfilesByAccount(
            @Parameter(description = "Account UUID") @PathVariable UUID accountId) {
        List<ProfileResult> result = getProfilesByAccountUseCase.getByAccountId(accountId);
        return ResponseEntity.ok(result);
    }

    private String extractExternalId(java.security.Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
