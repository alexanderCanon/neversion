package com.neversion.api.assignment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentContextResolver Unit Tests")
class AssignmentContextResolverUT {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private ReservationRepositoryPort reservationRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;

    private AssignmentContextResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AssignmentContextResolver(
                userRepositoryPort,
                vendorRepositoryPort,
                reservationRepositoryPort,
                serviceRepositoryPort
        );
    }

    @Test
    @DisplayName("resolveCallerVendor - should return vendor when user and vendor profile exist")
    void resolveCallerVendor_shouldReturnVendor_whenUserAndVendorExist() {
        String extId = "auth|test-user";
        User user = User.builder().id(1L).externalId(extId).build();
        Vendor vendor = Vendor.builder().id(10L).userId(1L).build();

        when(userRepositoryPort.findByExternalId(extId)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(vendor));

        Vendor result = resolver.resolveCallerVendor(extId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("resolveCallerVendor - should throw ResourceNotFoundException when user does not exist")
    void resolveCallerVendor_shouldThrowException_whenUserNotFound() {
        String extId = "auth|missing";
        when(userRepositoryPort.findByExternalId(extId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveCallerVendor(extId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Caller user not found");
    }

    @Test
    @DisplayName("resolveCallerVendor - should throw ResourceNotFoundException when vendor profile does not exist")
    void resolveCallerVendor_shouldThrowException_whenVendorNotFound() {
        String extId = "auth|no-vendor";
        User user = User.builder().id(1L).externalId(extId).build();

        when(userRepositoryPort.findByExternalId(extId)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveCallerVendor(extId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vendor profile not found for caller");
    }

    @Test
    @DisplayName("ensureOrderOwnership - should succeed when order vendor ID matches caller vendor ID")
    void ensureOrderOwnership_shouldSucceed_whenVendorMatches() {
        Order order = Order.builder().vendorId(10L).build();
        Vendor vendor = Vendor.builder().id(10L).build();

        resolver.ensureOrderOwnership(order, vendor);
        // Succeeded if no exception thrown
    }

    @Test
    @DisplayName("ensureOrderOwnership - should throw AccessDeniedException when vendor IDs mismatch")
    void ensureOrderOwnership_shouldThrowException_whenVendorMismatch() {
        Order order = Order.builder().vendorId(20L).build();
        Vendor vendor = Vendor.builder().id(10L).build();

        assertThatThrownBy(() -> resolver.ensureOrderOwnership(order, vendor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to manage this order.");
    }

    @Test
    @DisplayName("resolveSingleServiceForOrder - should throw BadRequestException when order has no reservation ID")
    void resolveSingleServiceForOrder_shouldThrowException_whenNoReservationId() {
        Order order = Order.builder().reservationId(null).build();

        assertThatThrownBy(() -> resolver.resolveSingleServiceForOrder(order))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Order has no reservation origin.");
    }

    @Test
    @DisplayName("resolveSingleServiceForOrder - should throw ResourceNotFoundException when reservation is not found")
    void resolveSingleServiceForOrder_shouldThrowException_whenReservationNotFound() {
        Order order = Order.builder().reservationId(100L).build();
        when(reservationRepositoryPort.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveSingleServiceForOrder(order))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation not found for order.");
    }

    @Test
    @DisplayName("resolveSingleServiceForOrder - should throw BadRequestException when reservation has no items")
    void resolveSingleServiceForOrder_shouldThrowException_whenNoItems() {
        Order order = Order.builder().reservationId(100L).build();
        Reservation reservation = Reservation.builder().id(100L).details(Collections.emptyList()).build();

        when(reservationRepositoryPort.findById(100L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> resolver.resolveSingleServiceForOrder(order))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Assignment currently supports single-item orders only.");
    }

    @Test
    @DisplayName("resolveSingleServiceForOrder - should throw BadRequestException when reservation has multiple items")
    void resolveSingleServiceForOrder_shouldThrowException_whenMultipleItems() {
        Order order = Order.builder().reservationId(100L).build();
        ReservationDetail detail1 = new ReservationDetail(1L, UUID.randomUUID(), 100L, 5L, 1, new java.math.BigDecimal("50.00"), new java.math.BigDecimal("50.00"));
        ReservationDetail detail2 = new ReservationDetail(2L, UUID.randomUUID(), 100L, 6L, 1, new java.math.BigDecimal("50.00"), new java.math.BigDecimal("50.00"));
        Reservation reservation = Reservation.builder().id(100L).details(List.of(detail1, detail2)).build();

        when(reservationRepositoryPort.findById(100L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> resolver.resolveSingleServiceForOrder(order))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Assignment currently supports single-item orders only.");
    }

    @Test
    @DisplayName("resolveSingleServiceForOrder - should throw BadRequestException when item quantity is not 1")
    void resolveSingleServiceForOrder_shouldThrowException_whenQuantityNotOne() {
        Order order = Order.builder().reservationId(100L).build();
        ReservationDetail detail = new ReservationDetail(1L, UUID.randomUUID(), 100L, 5L, 2, new java.math.BigDecimal("50.00"), new java.math.BigDecimal("100.00"));
        Reservation reservation = Reservation.builder().id(100L).details(List.of(detail)).build();

        when(reservationRepositoryPort.findById(100L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> resolver.resolveSingleServiceForOrder(order))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Assignment currently supports single-item orders only.");
    }

    @Test
    @DisplayName("resolveSingleServiceForOrder - should load details from repository if not populated on reservation")
    void resolveSingleServiceForOrder_shouldLoadFromRepo_whenDetailsNull() {
        Order order = Order.builder().reservationId(100L).build();
        Reservation reservation = Reservation.builder().id(100L).details(null).build();
        ReservationDetail detail = new ReservationDetail(1L, UUID.randomUUID(), 100L, 5L, 1, new java.math.BigDecimal("50.00"), new java.math.BigDecimal("50.00"));
        Service service = Service.builder().id(5L).name("Netflix").build();

        when(reservationRepositoryPort.findById(100L)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.findDetailsByReservationId(100L)).thenReturn(List.of(detail));
        when(serviceRepositoryPort.findByInternalId(5L)).thenReturn(Optional.of(service));

        Service result = resolver.resolveSingleServiceForOrder(order);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("resolveSingleServiceForOrder - should throw ResourceNotFoundException when service does not exist")
    void resolveSingleServiceForOrder_shouldThrowException_whenServiceNotFound() {
        Order order = Order.builder().reservationId(100L).build();
        ReservationDetail detail = new ReservationDetail(1L, UUID.randomUUID(), 100L, 5L, 1, new java.math.BigDecimal("50.00"), new java.math.BigDecimal("50.00"));
        Reservation reservation = Reservation.builder().id(100L).details(List.of(detail)).build();

        when(reservationRepositoryPort.findById(100L)).thenReturn(Optional.of(reservation));
        when(serviceRepositoryPort.findByInternalId(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveSingleServiceForOrder(order))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Service not found for order.");
    }
}
