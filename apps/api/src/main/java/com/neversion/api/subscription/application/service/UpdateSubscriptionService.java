package com.neversion.api.subscription.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.subscription.application.port.in.UpdateSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

/**
 * CU-A06: Manages subscription lifecycle transitions.
 */
@Service
public class UpdateSubscriptionService implements UpdateSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;

    public UpdateSubscriptionService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
    }

    @Override
    @Transactional
    public Subscription suspend(UUID id) {
        Subscription subscription = findOrThrow(id);

        if (subscription.getStatus() != SubStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Only ACTIVE subscriptions can be suspended. Current: " + subscription.getStatus());
        }

        subscription.setStatus(SubStatus.SUSPENDED);
        Subscription saved = subscriptionRepositoryPort.save(subscription);

        profileRepositoryPort.findByInternalId(subscription.getProfileId()).ifPresent(profile -> {
            profile.setStatus(ProfileStatus.RESERVED);
            profileRepositoryPort.save(profile);
        });

        return saved;
    }

    @Override
    @Transactional
    public Subscription terminate(UUID id) {
        Subscription subscription = findOrThrow(id);

        if (subscription.getStatus() == SubStatus.CANCELLED) {
            throw new BusinessRuleException("Subscription is already cancelled.");
        }

        subscription.setStatus(SubStatus.CANCELLED);
        return subscriptionRepositoryPort.save(subscription);
    }

    private Subscription findOrThrow(UUID id) {
        return subscriptionRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + id));
    }
}
