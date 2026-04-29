package com.neversion.api.assignment.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@Component
class AssignmentContextResolver {

    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;

    AssignmentContextResolver(
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
    }

    Vendor resolveCallerVendor(String callerExternalId) {
        var caller = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));

        return vendorRepositoryPort.findByUserId(caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for caller"));
    }

    void ensureOrderOwnership(Order order, Vendor vendor) {
        if (!order.getVendorId().equals(vendor.getId())) {
            throw new AccessDeniedException("You do not have permission to manage this order.");
        }
    }

    Service resolveSingleServiceForOrder(Order order) {
        if (order.getReservationId() == null) {
            throw new BadRequestException("Order has no reservation origin.");
        }

        Reservation reservation = reservationRepositoryPort.findById(order.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for order."));

        List<ReservationDetail> details = reservation.getDetails() != null
                ? reservation.getDetails()
                : reservationRepositoryPort.findDetailsByReservationId(reservation.getId());

        if (details.size() != 1 || details.get(0).qty() == null || details.get(0).qty().intValue() != 1) {
            throw new BadRequestException("Assignment currently supports single-item orders only.");
        }

        Long serviceId = details.get(0).serviceId();
        return serviceRepositoryPort.findByInternalId(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for order."));
    }
}
