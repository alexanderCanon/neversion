package com.neversion.api.dashboard.application.port.out;

import java.time.LocalDate;
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
}
