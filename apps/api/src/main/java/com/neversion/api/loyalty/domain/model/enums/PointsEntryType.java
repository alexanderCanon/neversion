package com.neversion.api.loyalty.domain.model.enums;

/**
 * Type of movement recorded in the client points ledger.
 */
public enum PointsEntryType {
    /** Points earned from an approved order (vendor's earn_pct on order.total). */
    EARN,
    /** Points spent by the client as a discount at checkout. */
    REDEEM,
    /** Manual adjustment made by the vendor from the panel (+/-). */
    ADJUSTMENT,
    /** Compensating entry that reverses a previous EARN or REDEEM. */
    REVERSAL
}
