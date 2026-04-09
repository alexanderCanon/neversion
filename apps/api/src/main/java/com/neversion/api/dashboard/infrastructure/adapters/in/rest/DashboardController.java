package com.neversion.api.dashboard.infrastructure.adapters.in.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.dashboard.application.port.in.GetAccountsByProductUseCase;
import com.neversion.api.dashboard.application.port.in.GetProductsSummaryUseCase;
import com.neversion.api.dashboard.application.port.in.GetProfilesByAccountUseCase;
import com.neversion.api.dashboard.application.result.AccountGroupResult;
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

    public DashboardController(GetProductsSummaryUseCase getProductsSummaryUseCase,
            GetAccountsByProductUseCase getAccountsByProductUseCase,
            GetProfilesByAccountUseCase getProfilesByAccountUseCase) {
        this.getProductsSummaryUseCase = getProductsSummaryUseCase;
        this.getAccountsByProductUseCase = getAccountsByProductUseCase;
        this.getProfilesByAccountUseCase = getProfilesByAccountUseCase;
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
}
