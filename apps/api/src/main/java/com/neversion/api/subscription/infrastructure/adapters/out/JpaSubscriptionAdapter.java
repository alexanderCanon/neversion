package com.neversion.api.subscription.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

@Repository
public class JpaSubscriptionAdapter implements SubscriptionRepositoryPort {

    private final SpringDataSubscriptionRepository subscriptionRepo;
    private final SubscriptionPersistenceMapper subscriptionMapper;

    public JpaSubscriptionAdapter(SpringDataSubscriptionRepository subscriptionRepo,
            SubscriptionPersistenceMapper subscriptionMapper) {
        this.subscriptionRepo = subscriptionRepo;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity entity = subscriptionMapper.toEntity(subscription);
        SubscriptionEntity saved = subscriptionRepo.saveAndFlush(entity);
        return subscriptionMapper.toDomain(saved);
    }

    @Override
    public Optional<Subscription> findById(UUID uuid) {
        return subscriptionRepo.findByUuid(uuid).map(subscriptionMapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByInternalId(Long id) {
        return subscriptionRepo.findById(id).map(subscriptionMapper::toDomain);
    }

    @Override
    public List<Subscription> findByClientId(Long clientId) {
        return subscriptionRepo.findByClientId(clientId).stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findByProfileId(Long profileId) {
        return subscriptionRepo.findByProfileId(profileId).stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findByStatus(SubStatus status) {
        return subscriptionRepo.findByStatus(status).stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findAll() {
        return subscriptionRepo.findAll().stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByProfileId(Long profileId) {
        return subscriptionRepo.existsByProfileIdAndStatus(profileId, SubStatus.ACTIVE);
    }

    @Override
    public List<Subscription> findOverdue(LocalDate asOf) {
        return subscriptionRepo.findOverdue(asOf).stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID uuid) {
        subscriptionRepo.findByUuid(uuid).ifPresent(e -> subscriptionRepo.deleteById(e.getId()));
    }
}
