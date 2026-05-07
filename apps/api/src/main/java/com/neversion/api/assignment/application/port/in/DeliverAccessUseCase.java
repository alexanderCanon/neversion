package com.neversion.api.assignment.application.port.in;

import com.neversion.api.subscription.domain.model.Subscription;

public interface DeliverAccessUseCase {
    void deliver(Subscription subscription);
}
