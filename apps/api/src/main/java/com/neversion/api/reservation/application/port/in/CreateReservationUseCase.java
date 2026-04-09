package com.neversion.api.reservation.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

public interface CreateReservationUseCase {

    Reservation create(UUID clientId, List<ReservationItemCommand> items);
}
