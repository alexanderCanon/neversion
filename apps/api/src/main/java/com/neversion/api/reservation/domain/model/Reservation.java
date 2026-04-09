package com.neversion.api.reservation.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Reservation {

    private UUID id;
    
    /** Internal DB PK of the Client — resolved from clientUuid before persistence. */
    private Long clientId;

    /** External identifier of the Client — received from the REST layer. */
    private UUID clientUuid;

    private BigDecimal discount;
    private BigDecimal total;
    private String receiptUrl;
    private ReservationStatus status;
    private Instant expirationDate;
    private Instant createdAt;
    private List<ReservationDetail> details;

    public Reservation() {
    }

    public Reservation(UUID id, Long clientId, UUID clientUuid, BigDecimal discount, BigDecimal total,
            String receiptUrl, ReservationStatus status, Instant expirationDate,
            Instant createdAt, List<ReservationDetail> details) {
        this.id = id;
        this.clientId = clientId;
        this.clientUuid = clientUuid;
        this.discount = discount;
        this.total = total;
        this.receiptUrl = receiptUrl;
        this.status = status;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
        this.details = details;
    }
}
