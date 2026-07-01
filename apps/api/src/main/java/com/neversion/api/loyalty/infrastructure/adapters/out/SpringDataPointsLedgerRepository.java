package com.neversion.api.loyalty.infrastructure.adapters.out;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;

public interface SpringDataPointsLedgerRepository extends JpaRepository<PointsLedgerEntity, Long> {

    @Query("SELECT COALESCE(SUM(e.points), 0) FROM PointsLedgerEntity e "
            + "WHERE e.clientId = :clientId AND e.status = :status")
    long sumPointsByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") PointsEntryStatus status);

    List<PointsLedgerEntity> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    long countByClientId(Long clientId);

    List<PointsLedgerEntity> findByOrderId(Long orderId);

    List<PointsLedgerEntity> findByReservationId(Long reservationId);
}
