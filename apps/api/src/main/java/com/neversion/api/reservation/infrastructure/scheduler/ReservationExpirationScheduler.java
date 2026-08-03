package com.neversion.api.reservation.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;

/**
 * BR-01: Automatically expires PENDING reservations that have exceeded
 * their 60-minute window. Runs every 5 minutes.
 * <p>
 * EXPIRED ≠ CANCELLED — EXPIRED is a system timeout,
 * CANCELLED is a manual action by admin or customer.
 * </p>
 * <p>
 * Note: When reservation-service (Rust) is active, background expiration is handled
 * by the Tokio worker in Rust. Enable this property only if running in monolith-only mode.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "neversion.cron.reservation-expiry.enabled", havingValue = "true", matchIfMissing = false)
public class ReservationExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationScheduler.class);

    private final ReservationRepositoryPort reservationRepositoryPort;

    public ReservationExpirationScheduler(ReservationRepositoryPort reservationRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    @Transactional
    public void expireTimedOutReservations() {
        int expiredCount = reservationRepositoryPort.expirePendingReservations();
        if (expiredCount > 0) {
            log.info("Expired {} pending reservation(s) that exceeded their time window", expiredCount);
        }
    }
}
