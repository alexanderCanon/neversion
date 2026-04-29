package com.neversion.api.subscription.application.port.in;

public interface DetectExpiredSubscriptionsUseCase {

    /**
     * US-047: Suspends active subscriptions whose payment due date has arrived.
     *
     * @return number of subscriptions suspended
     */
    int detectAndSuspend();
}
