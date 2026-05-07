package com.neversion.api.assignment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.assignment.application.port.in.dto.AssignmentSuggestion;
import com.neversion.api.exception.BadRequestException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.vendor.domain.model.Vendor;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuggestAssignmentService Unit Tests")
class SuggestAssignmentServiceUT {

    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;
    @Mock private AssignmentContextResolver contextResolver;

    private SuggestAssignmentService service;

    private final UUID orderUuid = UUID.randomUUID();
    private final Vendor vendor = Vendor.builder().id(7L).build();
    private final Order order = Order.builder()
            .id(11L)
            .uuid(orderUuid)
            .vendorId(7L)
            .status(OrderStatus.VALIDATED)
            .build();
    private final Service orderedService = Service.builder()
            .id(30L)
            .name("Netflix")
            .build();

    @BeforeEach
    void setUp() {
        service = new SuggestAssignmentService(
                orderRepositoryPort,
                accountRepositoryPort,
                profileRepositoryPort,
                notificationLogPort,
                contextResolver,
                new NotificationPayloadWriter(new ObjectMapper().findAndRegisterModules()));
    }

    @Test
    @DisplayName("suggest_byProfile_shouldReturnSuggestion_whenAvailableProfileExists")
    void suggest_byProfile_shouldReturnSuggestion_whenAvailableProfileExists() {
        UUID accountUuid = UUID.randomUUID();
        UUID profileUuid = UUID.randomUUID();

        givenValidatedOrder();
        when(accountRepositoryPort.findByServiceIdAndVendorId(30L, 7L)).thenReturn(List.of(Account.builder()
                .id(20L)
                .uuid(accountUuid)
                .saleMode(SaleMode.BY_PROFILE)
                .email("stream@example.com")
                .build()));
        when(profileRepositoryPort.findAvailableByAccountId(20L)).thenReturn(List.of(Profile.builder()
                .id(50L)
                .uuid(profileUuid)
                .status(ProfileStatus.AVAILABLE)
                .build()));

        AssignmentSuggestion result = service.suggest(orderUuid, "caller");

        assertThat(result.hasSuggestion()).isTrue();
        assertThat(result.saleMode()).isEqualTo(SaleMode.BY_PROFILE);
        assertThat(result.suggestedProfileUuid()).isEqualTo(profileUuid);
        assertThat(result.suggestedAccountUuid()).isEqualTo(accountUuid);
        assertThat(result.accountEmail()).isEqualTo("stream@example.com");
    }

    @Test
    @DisplayName("suggest_byProfile_shouldReturnNoSuggestion_andLogAlert_whenNoInventory")
    void suggest_byProfile_shouldReturnNoSuggestion_andLogAlert_whenNoInventory() {
        givenValidatedOrder();
        when(accountRepositoryPort.findByServiceIdAndVendorId(30L, 7L)).thenReturn(List.of(Account.builder()
                .id(20L)
                .saleMode(SaleMode.BY_PROFILE)
                .build()));
        when(profileRepositoryPort.findAvailableByAccountId(20L)).thenReturn(List.of());

        AssignmentSuggestion result = service.suggest(orderUuid, "caller");

        assertThat(result.hasSuggestion()).isFalse();
        assertThat(result.noInventoryReason()).isEqualTo("NO_AVAILABLE_PROFILE");
        verify(notificationLogPort).record(eq("NO_INVENTORY_ALERT"), eq("vendor:7"), org.mockito.ArgumentMatchers.contains("\"vendorId\":7"),
                eq("vendor"), eq(7L), eq("no_inventory"));
    }

    @Test
    @DisplayName("suggest_fullAccount_shouldReturnOwnerProfileSuggestion_whenAvailableAccountExists")
    void suggest_fullAccount_shouldReturnOwnerProfileSuggestion_whenAvailableAccountExists() {
        UUID accountUuid = UUID.randomUUID();
        UUID ownerProfileUuid = UUID.randomUUID();

        givenValidatedOrder();
        when(accountRepositoryPort.findByServiceIdAndVendorId(30L, 7L)).thenReturn(List.of(Account.builder()
                .id(20L)
                .uuid(accountUuid)
                .saleMode(SaleMode.FULL_ACCOUNT)
                .email("full@example.com")
                .status(com.neversion.api.shared.domain.model.enums.AccountStatus.AVAILABLE)
                .build()));
        when(profileRepositoryPort.findByAccountId(20L)).thenReturn(List.of(Profile.builder()
                .id(50L)
                .uuid(ownerProfileUuid)
                .isOwner(true)
                .status(ProfileStatus.AVAILABLE)
                .build()));

        AssignmentSuggestion result = service.suggest(orderUuid, "caller");

        assertThat(result.hasSuggestion()).isTrue();
        assertThat(result.saleMode()).isEqualTo(SaleMode.FULL_ACCOUNT);
        assertThat(result.suggestedProfileUuid()).isEqualTo(ownerProfileUuid);
        assertThat(result.suggestedAccountUuid()).isEqualTo(accountUuid);
        assertThat(result.accountEmail()).isEqualTo("full@example.com");
    }

    @Test
    @DisplayName("suggest_shouldThrowBusinessRule_whenOrderNotValidated")
    void suggest_shouldThrowBusinessRule_whenOrderNotValidated() {
        when(contextResolver.resolveCallerVendor("caller")).thenReturn(vendor);
        when(orderRepositoryPort.findByUuid(orderUuid)).thenReturn(Optional.of(Order.builder()
                .id(11L)
                .uuid(orderUuid)
                .vendorId(7L)
                .status(OrderStatus.PENDING)
                .build()));

        assertThatThrownBy(() -> service.suggest(orderUuid, "caller"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("VALIDATED");
    }

    private void givenValidatedOrder() {
        when(contextResolver.resolveCallerVendor("caller")).thenReturn(vendor);
        when(orderRepositoryPort.findByUuid(orderUuid)).thenReturn(Optional.of(order));
        when(contextResolver.resolveSingleServiceForOrder(order)).thenReturn(orderedService);
    }
}
