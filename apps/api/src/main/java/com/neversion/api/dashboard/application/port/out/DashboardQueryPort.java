package com.neversion.api.dashboard.application.port.out;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.neversion.api.dashboard.application.result.ExpiringSubscriptionResult;
import com.neversion.api.dashboard.application.result.InventoryAvailabilityResult;
import com.neversion.api.dashboard.application.result.ProfileResult;
import com.neversion.api.dashboard.application.result.ProductSummaryResult;

/**
 * Outbound port for dashboard read-only queries.
 * Implemented by the persistence adapter using native SQL.
 */
public interface DashboardQueryPort {

    /**
     * Endpoint 1: products with account count filtered by category.
     */
    List<ProductSummaryResult> findProductsByCategory(String category);

    /**
     * Endpoint 2: raw account data for a product.
     * Returns a list of maps with keys: accountId, email, password, cutOffDate,
     * accountType, accountStatus, maxProfiles, occupiedProfiles.
     */
    List<Map<String, Object>> findAccountsByProductId(UUID productId);

    /**
     * Endpoint 3: profiles for an account with subscription and customer data.
     */
    List<ProfileResult> findProfilesByAccountId(UUID accountId);

    /**
     * US-063: Vendor-scoped subscriptions due in the requested date window.
     */
    List<ExpiringSubscriptionResult> findExpiringSubscriptions(
            Long vendorId,
            LocalDate from,
            LocalDate to);

    /**
     * US-064: Vendor-scoped inventory availability grouped by service.
     */
    List<InventoryAvailabilityResult> findInventoryAvailability(Long vendorId);

    /**
     * US-065: Vendor-scoped unique clients with at least one active subscription.
     */
    long countActiveClients(Long vendorId);

    /**
     * US-066: Vendor-scoped successful renewal orders in the requested period.
     */
    long countSuccessfulRenewals(Long vendorId, OffsetDateTime periodStart, OffsetDateTime nextPeriodStart);

    /**
     * US-067: Vendor-scoped gross profit from created subscriptions and successful renewals.
     */
    BigDecimal calculateGrossProfit(Long vendorId, OffsetDateTime periodStart, OffsetDateTime nextPeriodStart);
}
