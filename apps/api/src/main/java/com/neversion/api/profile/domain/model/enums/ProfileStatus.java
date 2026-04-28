package com.neversion.api.profile.domain.model.enums;

/**
 * Operational status of a profile.
 *
 * Manually settable (US-027): AVAILABLE, BLOCKED.
 * System-controlled only (EPIC-06/07): ACTIVE, RESERVED, OCCUPIED, EXPIRED.
 */
public enum ProfileStatus {
    /** Profile is unassigned and ready to sell. */
    AVAILABLE,
    /** Profile is currently linked to an active subscription. Controlled by EPIC-07. */
    ACTIVE,
    /** Profile is held for a pending order. Controlled by EPIC-06. */
    RESERVED,
    /** Profile was previously active; subscription has ended. Controlled by EPIC-07. */
    OCCUPIED,
    /** Manually blocked by the vendor (US-027). */
    BLOCKED,
    /** Account expired — profile unavailable. Controlled by EPIC-07. */
    EXPIRED
}
