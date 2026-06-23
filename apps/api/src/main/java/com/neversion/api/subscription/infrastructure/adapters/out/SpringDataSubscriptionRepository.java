package com.neversion.api.subscription.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neversion.api.subscription.domain.model.enums.SubStatus;

public interface SpringDataSubscriptionRepository
        extends JpaRepository<SubscriptionEntity, Long>, JpaSpecificationExecutor<SubscriptionEntity> {

    Optional<SubscriptionEntity> findByUuid(UUID uuid);

    Optional<SubscriptionEntity> findByOrderId(Long orderId);

    List<SubscriptionEntity> findByClientId(Long clientId);

    List<SubscriptionEntity> findByProfileId(Long profileId);

    List<SubscriptionEntity> findByStatus(SubStatus status);

    /** Overbooking guard (BR-04): a profile can only have one active subscription. */
    boolean existsByProfileIdAndStatus(Long profileId, SubStatus status);

    /**
     * Returns all subscriptions whose payment_due_date is on or before the given date.
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.paymentDueDate <= :asOf AND s.status = :status")
    List<SubscriptionEntity> findOverdue(@Param("asOf") LocalDate asOf, @Param("status") SubStatus status);

    /** US-054: Finds active subscriptions due on a specific date for renewal reminders. */
    List<SubscriptionEntity> findByPaymentDueDateAndStatus(LocalDate paymentDueDate, SubStatus status);

}
