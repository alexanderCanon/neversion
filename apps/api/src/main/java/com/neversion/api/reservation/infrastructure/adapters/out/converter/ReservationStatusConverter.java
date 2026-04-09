package com.neversion.api.reservation.infrastructure.adapters.out.converter;

import com.neversion.api.infrastructure.EnumConverter;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReservationStatusConverter extends EnumConverter<ReservationStatus> {

    public ReservationStatusConverter() {
        super(ReservationStatus.class);
    }
}
