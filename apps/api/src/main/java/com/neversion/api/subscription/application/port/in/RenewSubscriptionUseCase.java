package com.neversion.api.subscription.application.port.in;

import java.util.UUID;

import com.neversion.api.subscription.domain.model.Subscription;

public interface RenewSubscriptionUseCase {

    /**
     * US-045: Renews an active or suspended subscription owned by the caller.
     */
    Subscription renew(UUID subscriptionUuid, String callerExternalId);
}
