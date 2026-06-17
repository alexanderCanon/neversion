package com.neversion.api.subscription.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neversion.api.subscription.application.port.in.DetectExpiredSubscriptionsUseCase;

@Component
@ConditionalOnProperty(
        prefix = "neversion.cron.subscription-expiry",
        name = "enabled",
        havingValue = "true")
public class SubscriptionExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryScheduler.class);

    private final DetectExpiredSubscriptionsUseCase detectExpiredSubscriptionsUseCase;

    public SubscriptionExpiryScheduler(DetectExpiredSubscriptionsUseCase detectExpiredSubscriptionsUseCase) {
        this.detectExpiredSubscriptionsUseCase = detectExpiredSubscriptionsUseCase;
    }

    @Scheduled(cron = "${neversion.cron.subscription-expiry.schedule:0 0 8 * * *}")
    public void detectExpiredSubscriptions() {
        int suspendedCount = detectExpiredSubscriptionsUseCase.detectAndSuspend();
        if (suspendedCount > 0) {
            log.info("Suspended {} expired subscription(s).", suspendedCount);
        }
    }
}
