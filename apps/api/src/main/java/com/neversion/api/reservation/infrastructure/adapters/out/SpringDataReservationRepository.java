package com.neversion.api.reservation.infrastructure.adapters.out;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

public interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, UUID> {

  boolean existsByReceiptUrl(String receiptUrl);

  List<ReservationEntity> findByStatus(ReservationStatus status);

  /**
   * Bulk-update: sets PENDING reservations past their expiration to EXPIRED.
   */
  @Modifying
  @Query(value = """
      UPDATE reservations SET status = 'expired'
      WHERE status = 'pending'
        AND expiration_date < NOW()
      """, nativeQuery = true)
  int expirePendingReservations();
}
