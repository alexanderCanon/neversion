package com.neversion.api.subscription.application.port.in;

import com.neversion.api.subscription.domain.model.Subscription;

/**
 * CU-A05: Admin manually assigns a Client to a Profile, creating an active Subscription.
 */
public interface AssignSubscriptionUseCase {

    Subscription assign(Subscription subscription);
}
