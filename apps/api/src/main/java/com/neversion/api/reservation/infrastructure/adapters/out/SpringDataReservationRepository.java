package com.neversion.api.reservation.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

/**
 * US-009: PK Long (BIGINT IDENTITY).
 */
interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, Long> {

  boolean existsByReceiptUrl(String receiptUrl);

  Optional<ReservationEntity> findByUuid(UUID uuid);

  List<ReservationEntity> findByStatus(ReservationStatus status);

  @Modifying
  @Query(value = """
      UPDATE reservations SET status = 'expired'
      WHERE status = 'pending'
        AND expiration_date < NOW()
      """, nativeQuery = true)
  int expirePendingReservations();
}
