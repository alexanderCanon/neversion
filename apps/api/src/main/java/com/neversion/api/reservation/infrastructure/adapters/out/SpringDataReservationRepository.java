package com.neversion.api.reservation.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

/**
 * US-009: PK Long (BIGINT IDENTITY).
 */
interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, Long> {

  boolean existsByReceiptUrl(String receiptUrl);

  Optional<ReservationEntity> findByUuid(UUID uuid);

  List<ReservationEntity> findByClientIdOrderByCreatedAtDesc(Long clientId);

  List<ReservationEntity> findByStatus(ReservationStatus status);

  @Query(value = """
      SELECT EXISTS (
        SELECT 1
        FROM reservations
        WHERE renewal_subscription_id = :subscriptionId
          AND status IN ('pending', 'uploaded')
      )
      """, nativeQuery = true)
  boolean existsActiveRenewalBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

  @Modifying
  @Query(value = """
      UPDATE reservations SET status = 'expired'
      WHERE status = 'pending'
        AND expiration_date < NOW()
      """, nativeQuery = true)
  int expirePendingReservations();
}
