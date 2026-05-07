package com.neversion.api.subscription.domain.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * BR-07: Calculates the next due date for late subscription renewals.
 */
public class SubscriptionRenewalDomainService {

    private static final int STANDARD_RENEWAL_DAYS = 30;

    public LocalDate calculateNewDueDate(LocalDate currentDueDate, LocalDate paymentDate,
            int gracePeriodDays) {
        long daysOverdue = ChronoUnit.DAYS.between(currentDueDate, paymentDate);

        if (daysOverdue <= gracePeriodDays) {
            return currentDueDate.plusDays(STANDARD_RENEWAL_DAYS);
        }

        return paymentDate.plusDays(STANDARD_RENEWAL_DAYS);
    }
}
