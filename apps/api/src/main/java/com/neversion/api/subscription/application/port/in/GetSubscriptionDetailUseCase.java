package com.neversion.api.subscription.application.port.in;

import java.util.UUID;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.subscription.domain.model.Subscription;

public interface GetSubscriptionDetailUseCase {

    /**
     * US-044: Returns the full subscription detail for the authenticated vendor.
     */
    SubscriptionDetail getDetail(UUID subscriptionUuid, String callerExternalId);

    record SubscriptionDetail(
            Subscription subscription,
            Client client,
            Profile profile,
            Account account,
            Service service,
            Order order) {
    }
}
