package com.neversion.api.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.loyalty.application.port.in.ReversePointsUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeOrderStatusService unit tests — US-038/039")
class ChangeOrderStatusServiceUT {

    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private OrderStatusHistoryPort orderStatusHistoryPort;
    @Mock private NotificationLogPort notificationLogPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private ReversePointsUseCase reversePointsUseCase;

    private ChangeOrderStatusService changeOrderStatusService;

    private static final UUID ORDER_UUID = UUID.randomUUID();
    private static final String CALLER_EXTERNAL_ID = "supabase-id-123";
    private static final Long VENDOR_ID = 5L;
    private static final Long USER_ID = 10L;
    private static final Long CLIENT_ID = 20L;

    @BeforeEach
    void setUp() {
        changeOrderStatusService = new ChangeOrderStatusService(
                orderRepositoryPort, orderStatusHistoryPort, notificationLogPort,
                userRepositoryPort, vendorRepositoryPort, clientRepositoryPort, reversePointsUseCase);
    }

    private User buildUser() {
        return User.builder().id(USER_ID).externalId(CALLER_EXTERNAL_ID).build();
    }

    private Vendor buildVendor(Long id) {
        return Vendor.builder().id(id).userId(USER_ID).build();
    }

    private Order buildOrder(OrderStatus status, Long vendorId) {
        return Order.builder()
                .id(1L)
                .uuid(ORDER_UUID)
                .vendorId(vendorId)
                .clientId(CLIENT_ID)
                .status(status)
                .build();
    }

    private Client buildClient() {
        return Client.builder().id(CLIENT_ID).email("client@test.com").name("Juan Perez").build();
    }

    private void mockCallerResolution(Long callerVendorId) {
        when(userRepositoryPort.findByExternalId(CALLER_EXTERNAL_ID)).thenReturn(Optional.of(buildUser()));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildVendor(callerVendorId)));
    }

    @Test
    @DisplayName("changeStatus - should complete order, record audit, and notify")
    void changeStatus_complete_shouldTransitionAuditAndNotify() {
        // Given
        mockCallerResolution(VENDOR_ID);
        Order order = buildOrder(OrderStatus.VALIDATED, VENDOR_ID);

        when(orderRepositoryPort.findByUuid(ORDER_UUID)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(clientRepositoryPort.findByInternalId(CLIENT_ID)).thenReturn(Optional.of(buildClient()));

        // When
        Order result = changeOrderStatusService.changeStatus(ORDER_UUID, OrderStatus.COMPLETED, "Done", CALLER_EXTERNAL_ID);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.getNotes()).isEqualTo("Done");
        verify(orderStatusHistoryPort).record(any(OrderStatusChange.class));
        verify(notificationLogPort).record(eq("ORDER_COMPLETED"), eq("client@test.com"), any(String.class),
                eq("order"), any(), eq("completed"));
    }

    @Test
    @DisplayName("changeStatus - should cancel order, record audit, and notify")
    void changeStatus_cancel_shouldTransitionAuditAndNotify() {
        // Given
        mockCallerResolution(VENDOR_ID);
        Order order = buildOrder(OrderStatus.VALIDATED, VENDOR_ID);

        when(orderRepositoryPort.findByUuid(ORDER_UUID)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(clientRepositoryPort.findByInternalId(CLIENT_ID)).thenReturn(Optional.of(buildClient()));

        // When
        Order result = changeOrderStatusService.changeStatus(ORDER_UUID, OrderStatus.CANCELLED, "Customer asked", CALLER_EXTERNAL_ID);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderStatusHistoryPort).record(any(OrderStatusChange.class));
        verify(notificationLogPort).record(eq("ORDER_CANCELLED"), eq("client@test.com"), any(String.class),
                eq("order"), any(), eq("cancelled"));
    }

    @Test
    @DisplayName("changeStatus - should throw AccessDeniedException when caller is not owner")
    void changeStatus_notOwner_shouldThrow403() {
        // Given
        mockCallerResolution(999L);
        Order order = buildOrder(OrderStatus.VALIDATED, VENDOR_ID);

        when(orderRepositoryPort.findByUuid(ORDER_UUID)).thenReturn(Optional.of(order));

        // When / Then
        assertThatThrownBy(() -> changeOrderStatusService.changeStatus(ORDER_UUID, OrderStatus.COMPLETED, null, CALLER_EXTERNAL_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("changeStatus - should throw BusinessRuleException when order already finalized")
    void changeStatus_finalized_shouldThrow400() {
        // Given
        mockCallerResolution(VENDOR_ID);
        Order order = buildOrder(OrderStatus.COMPLETED, VENDOR_ID);

        when(orderRepositoryPort.findByUuid(ORDER_UUID)).thenReturn(Optional.of(order));

        // When / Then
        assertThatThrownBy(() -> changeOrderStatusService.changeStatus(ORDER_UUID, OrderStatus.CANCELLED, null, CALLER_EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("finalized order");
    }
}
