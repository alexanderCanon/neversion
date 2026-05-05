package com.neversion.api.account.application.service;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListAccountsService unit tests")
class ListAccountsServiceUT {

    @Mock
    private AccountRepositoryPort accountRepositoryPort;
    @Mock
    private VendorRepositoryPort vendorRepositoryPort;
    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;

    private ListAccountsService service;

    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final String EXTERNAL_ID = "supabase-vendor";
    private static final Long USER_ID = 1L;
    private static final Long VENDOR_ID = 10L;

    @BeforeEach
    void setUp() {
        service = new ListAccountsService(
                accountRepositoryPort,
                vendorRepositoryPort,
                serviceRepositoryPort,
                userRepositoryPort);
    }

    @Test
    @DisplayName("listByVendor - should return accounts when caller owns vendor")
    void listByVendor_shouldReturnAccounts_whenCallerOwnsVendor() {
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(buildVendor(VENDOR_ID)));
        mockCallerVendor(VENDOR_ID);
        Account account = Account.builder().id(1L).vendorId(VENDOR_ID).build();
        when(accountRepositoryPort.findByVendorId(VENDOR_ID)).thenReturn(List.of(account));

        List<Account> result = service.listByVendor(VENDOR_UUID, null, null, EXTERNAL_ID);

        assertThat(result).containsExactly(account);
        verify(accountRepositoryPort).findByVendorId(VENDOR_ID);
    }

    @Test
    @DisplayName("listByVendor - should throw AccessDeniedException when caller does not own vendor")
    void listByVendor_shouldThrow_whenCallerDoesNotOwnVendor() {
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(buildVendor(99L)));
        mockCallerVendor(VENDOR_ID);

        assertThatThrownBy(() -> service.listByVendor(VENDOR_UUID, null, null, EXTERNAL_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("listByVendor - should throw ResourceNotFoundException when vendor is missing")
    void listByVendor_shouldThrow_whenVendorMissing() {
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listByVendor(VENDOR_UUID, null, AccountStatus.AVAILABLE, EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void mockCallerVendor(Long vendorId) {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildVendor(vendorId)));
    }

    private Vendor buildVendor(Long vendorId) {
        return Vendor.builder()
                .id(vendorId)
                .uuid(VENDOR_UUID)
                .userId(USER_ID)
                .storeName("Mi Tienda")
                .build();
    }
}
