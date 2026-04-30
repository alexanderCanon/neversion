package com.neversion.api.dashboard.infrastructure.adapters.in.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.dashboard.application.port.in.GetAccountsByProductUseCase;
import com.neversion.api.dashboard.application.port.in.GetExpiringSubscriptionsKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetInventoryAvailabilityKpiUseCase;
import com.neversion.api.dashboard.application.port.in.GetProductsSummaryUseCase;
import com.neversion.api.dashboard.application.port.in.GetProfilesByAccountUseCase;
import com.neversion.api.dashboard.application.result.AccountGroupResult;
import com.neversion.api.dashboard.application.result.ExpiringSubscriptionsKpiResult;
import com.neversion.api.dashboard.application.result.InventoryAvailabilityResult;
import com.neversion.api.dashboard.application.result.ProfileResult;
import com.neversion.api.dashboard.application.result.ProductSummaryResult;
import com.neversion.api.shared.domain.model.enums.CategoryType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Master dashboard for admin monitoring")
public class DashboardController {

    private final GetProductsSummaryUseCase getProductsSummaryUseCase;
    private final GetAccountsByProductUseCase getAccountsByProductUseCase;
    private final GetProfilesByAccountUseCase getProfilesByAccountUseCase;
    private final GetExpiringSubscriptionsKpiUseCase getExpiringSubscriptionsKpiUseCase;
    private final GetInventoryAvailabilityKpiUseCase getInventoryAvailabilityKpiUseCase;

    public DashboardController(GetProductsSummaryUseCase getProductsSummaryUseCase,
            GetAccountsByProductUseCase getAccountsByProductUseCase,
            GetProfilesByAccountUseCase getProfilesByAccountUseCase,
            GetExpiringSubscriptionsKpiUseCase getExpiringSubscriptionsKpiUseCase,
            GetInventoryAvailabilityKpiUseCase getInventoryAvailabilityKpiUseCase) {
        this.getProductsSummaryUseCase = getProductsSummaryUseCase;
        this.getAccountsByProductUseCase = getAccountsByProductUseCase;
        this.getProfilesByAccountUseCase = getProfilesByAccountUseCase;
        this.getExpiringSubscriptionsKpiUseCase = getExpiringSubscriptionsKpiUseCase;
        this.getInventoryAvailabilityKpiUseCase = getInventoryAvailabilityKpiUseCase;
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
