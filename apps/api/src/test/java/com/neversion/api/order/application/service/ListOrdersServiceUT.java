package com.neversion.api.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListOrdersService unit tests — US-037")
class ListOrdersServiceUT {

    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;

    private ListOrdersService listOrdersService;

    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final String CALLER_EXTERNAL_ID = "supabase-id-123";
    private static final Long VENDOR_ID = 5L;
    private static final Long USER_ID = 10L;
    private static final Long CLIENT_ID = 20L;

    @BeforeEach
    void setUp() {
        listOrdersService = new ListOrdersService(
                orderRepositoryPort, vendorRepositoryPort, clientRepositoryPort, userRepositoryPort);
    }

    private User buildUser() {
        return User.builder().id(USER_ID).externalId(CALLER_EXTERNAL_ID).build();
    }

    private Vendor buildVendor(Long id) {
        return Vendor.builder().id(id).userId(USER_ID).uuid(VENDOR_UUID).build();
    }

    private Client buildClient() {
        return Client.builder().id(CLIENT_ID).uuid(CLIENT_UUID).build();
    }

    private void mockCallerResolution(Long callerVendorId) {
        when(userRepositoryPort.findByExternalId(CALLER_EXTERNAL_ID)).thenReturn(Optional.of(buildUser()));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildVendor(callerVendorId)));
    }

    @Test
    @DisplayName("listByVendor - should return filtered orders when owner calls")
    void listByVendor_validOwner_shouldReturnOrders() {
        // Given
        mockCallerResolution(VENDOR_ID);
        Vendor targetVendor = buildVendor(VENDOR_ID);
        Client client = buildClient();
        List<Order> expectedOrders = List.of(Order.builder().id(1L).build());

        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(targetVendor));
        when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(client));
        when(orderRepositoryPort.findByVendorIdFiltered(VENDOR_ID, CLIENT_ID, OrderStatus.VALIDATED))
                .thenReturn(expectedOrders);

        // When
        List<Order> result = listOrdersService.listByVendor(
                VENDOR_UUID, CLIENT_UUID, OrderStatus.VALIDATED, CALLER_EXTERNAL_ID);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listByVendor - should throw AccessDeniedException when caller is not the owner")
    void listByVendor_notOwner_shouldThrow403() {
        // Given
        mockCallerResolution(999L); // Different vendor ID
        Vendor targetVendor = buildVendor(VENDOR_ID);

        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(targetVendor));

        // When / Then
        assertThatThrownBy(() -> listOrdersService.listByVendor(
                VENDOR_UUID, CLIENT_UUID, OrderStatus.VALIDATED, CALLER_EXTERNAL_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("do not have permission");
    }

    @Test
    @DisplayName("listByVendor - should throw ResourceNotFoundException when vendor not found")
    void listByVendor_vendorNotFound_shouldThrow404() {
        // Given
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> listOrdersService.listByVendor(
                VENDOR_UUID, CLIENT_UUID, OrderStatus.VALIDATED, CALLER_EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vendor not found");
    }
}
