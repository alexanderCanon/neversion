package com.neversion.api.subscription.application.port.in;

import com.neversion.api.subscription.domain.model.Subscription;

public interface CreateManualSubscriptionUseCase {

    /**
     * US-048: Creates a subscription without a previous order/reservation.
     */
    Subscription create(Subscription subscription, boolean sendNotification, String callerExternalId);
}
