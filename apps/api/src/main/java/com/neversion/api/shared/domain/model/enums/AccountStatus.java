package com.neversion.api.shared.domain.model.enums;

/**
 * Operational status of a master account (US-006).
 * Values persisted in lowercase per NFR-06.
 * - AVAILABLE: no subscriptions assigned yet
 * - PARTIAL: some profiles sold, others still free
 * - FULL: all profiles or full account sold
 * - EXPIRED: account renewal lapsed
 */
public enum AccountStatus {
    AVAILABLE,
    PARTIAL,
    FULL,
    EXPIRED
}
