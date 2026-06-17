package com.neversion.api.subscription.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neversion.api.subscription.application.port.in.ReconcileInventoryConsistencyUseCase;

/**
 * Tech-debt remediation A1 (Phase 1 — safety net).
 * <p>
 * Nightly job that detects inventory drift between subscriptions and profiles.
 * Alerting only — it never mutates data. Disabled by default; enable via
 * {@code neversion.cron.inventory-reconciliation.enabled=true}.
 */
@Component
@ConditionalOnProperty(
        prefix = "neversion.cron.inventory-reconciliation",
        name = "enabled",
        havingValue = "true")
public class InventoryReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(InventoryReconciliationScheduler.class);

    private final ReconcileInventoryConsistencyUseCase reconcileInventoryConsistencyUseCase;

    public InventoryReconciliationScheduler(
            ReconcileInventoryConsistencyUseCase reconcileInventoryConsistencyUseCase) {
        this.reconcileInventoryConsistencyUseCase = reconcileInventoryConsistencyUseCase;
    }

    @Scheduled(cron = "${neversion.cron.inventory-reconciliation.schedule:0 30 3 * * *}")
    public void reconcileInventory() {
        int inconsistencies = reconcileInventoryConsistencyUseCase.detectInconsistencies();
        if (inconsistencies == 0) {
            log.info("Inventory reconciliation completed: no inconsistencies detected.");
        }
    }
}
