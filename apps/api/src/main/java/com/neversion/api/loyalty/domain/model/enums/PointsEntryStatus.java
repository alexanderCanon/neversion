package com.neversion.api.loyalty.domain.model.enums;

/**
 * Status of a points ledger entry.
 * <p>
 * Today, entries are always created as {@code AVAILABLE} (points are credited
 * immediately when the reservation is validated — see ADR on rewards program).
 * {@code PENDING} is reserved for a future hold-period feature.
 */
public enum PointsEntryStatus {
    AVAILABLE,
    PENDING,
    CANCELLED
}
