package com.neversion.api.loyalty.application.port.in;

import java.math.BigDecimal;

/**
 * Credits loyalty points for an approved order, based on the vendor's
 * rewards_cfg.earn_pct applied to the order's net total.
 * Points are credited immediately as AVAILABLE (no pending hold for now).
 */
public interface EarnPointsUseCase {

    /**
     * @param orderId   internal order id (FK for the ledger entry)
     * @param clientId  internal client id who earns the points
     * @param vendorId  internal vendor id (for rewards_cfg lookup and tenancy)
     * @param orderTotal net total paid for the order (after discounts)
     */
    void earnForOrder(Long orderId, Long clientId, Long vendorId, BigDecimal orderTotal);
}
