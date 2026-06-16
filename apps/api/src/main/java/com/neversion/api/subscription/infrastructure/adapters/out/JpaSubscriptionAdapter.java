package com.neversion.api.subscription.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.neversion.api.account.infrastructure.adapters.out.AccountEntity;
import com.neversion.api.profile.infrastructure.adapters.out.ProfileEntity;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
public class JpaSubscriptionAdapter implements SubscriptionRepositoryPort {

    private final SpringDataSubscriptionRepository subscriptionRepo;
    private final SubscriptionPersistenceMapper subscriptionMapper;
    private final SubscriptionQueryRepository subscriptionQueryRepository;

    public JpaSubscriptionAdapter(SpringDataSubscriptionRepository subscriptionRepo,
            SubscriptionPersistenceMapper subscriptionMapper,
            SubscriptionQueryRepository subscriptionQueryRepository) {
        this.subscriptionRepo = subscriptionRepo;
        this.subscriptionMapper = subscriptionMapper;
        this.subscriptionQueryRepository = subscriptionQueryRepository;
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
    public Optional<Subscription> findByOrderId(Long orderId) {
        return subscriptionRepo.findByOrderId(orderId).map(subscriptionMapper::toDomain);
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
    public List<Subscription> findByVendorIdFiltered(Long vendorId, Long serviceId, SubStatus status) {
        return subscriptionRepo.findAll(
                        subscriptionFilter(vendorId, serviceId, status),
                        Sort.by(Sort.Direction.ASC, "paymentDueDate"))
                .stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public List<SubscriptionListView> findVendorSubscriptionViews(
            Long vendorId, Long serviceId, SubStatus status) {
        return subscriptionQueryRepository.findVendorSubscriptionViews(vendorId, serviceId, status);
    }

    private Specification<SubscriptionEntity> subscriptionFilter(
            Long vendorId, Long serviceId, SubStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("vendorId"), vendorId));

            if (serviceId != null) {
                Subquery<Long> profileIds = query.subquery(Long.class);
                Root<ProfileEntity> profile = profileIds.from(ProfileEntity.class);
                Root<AccountEntity> account = profileIds.from(AccountEntity.class);

                profileIds.select(profile.get("id"))
                        .where(
                                criteriaBuilder.equal(profile.get("accountId"), account.get("id")),
                                criteriaBuilder.equal(account.get("serviceId"), serviceId));

                predicates.add(root.get("profileId").in(profileIds));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
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
        return subscriptionRepo.findOverdue(asOf, SubStatus.ACTIVE).stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findActiveByPaymentDueDate(LocalDate dueDate) {
        return subscriptionRepo.findByPaymentDueDateAndStatus(dueDate, SubStatus.ACTIVE).stream()
                .map(subscriptionMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID uuid) {
        subscriptionRepo.findByUuid(uuid).ifPresent(e -> subscriptionRepo.deleteById(e.getId()));
    }
}
