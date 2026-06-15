package com.neversion.api.subscription.application.port.in;

/**
 * Tech-debt remediation A1 (Phase 1 — safety net).
 * <p>
 * Detects inventory inconsistencies between subscriptions and their assigned
 * profiles, logging each finding at WARN level. This is an <b>alerting-only</b>
 * use case: it never mutates state, so it is safe to run unattended.
 */
public interface ReconcileInventoryConsistencyUseCase {

    /**
     * Scans ACTIVE and SUSPENDED subscriptions and reports any whose profile
     * is in an unexpected state.
     *
     * @return the number of inconsistencies detected (and logged)
     */
    int detectInconsistencies();
}
