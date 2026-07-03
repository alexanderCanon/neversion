package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateManualSubscriptionService — US-048 unit tests")
class CreateManualSubscriptionServiceUT {

    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private DeliverAccessUseCase deliverAccessUseCase;
    @Mock private ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;

    private CreateManualSubscriptionService createManualSubscriptionService;

    private static final String EXTERNAL_ID = "auth|manual-sub";
    private static final Long USER_ID = 5L;
    private static final Long VENDOR_ID = 10L;
    private static final Long CLIENT_ID = 20L;
    private static final Long PROFILE_ID = 30L;
    private static final Long ACCOUNT_ID = 40L;
    private static final Long SERVICE_ID = 50L;
    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final UUID PROFILE_UUID = UUID.randomUUID();
    private static final UUID SERVICE_UUID = UUID.randomUUID();
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-29T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        createManualSubscriptionService = new CreateManualSubscriptionService(
                subscriptionRepositoryPort,
                clientRepositoryPort,
                profileRepositoryPort,
                accountRepositoryPort,
                serviceRepositoryPort,
                deliverAccessUseCase,
                new VendorSecurityService(userRepositoryPort, vendorRepositoryPort),
                historyRepositoryPort,
                FIXED_CLOCK);
    }

    private void mockOwnershipResolution() {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).role(UserRole.VENDOR).build();
        Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).storeName("Vendor").build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private Subscription buildInput() {
        return Subscription.builder()
                .clientUuid(CLIENT_UUID)
                .profileUuid(PROFILE_UUID)
                .serviceUuid(SERVICE_UUID)
                .paymentDueDate(LocalDate.of(2026, 5, 29))
                .priceSold(new BigDecimal("100.00"))
                .discountApplied(new BigDecimal("5.00"))
                .notes("External sale")
                .build();
    }

    private Client buildClient(Long vendorId) {
        return Client.builder().id(CLIENT_ID).uuid(CLIENT_UUID).vendorId(vendorId).email("client@test.com").build();
    }

    private Service buildService(Long vendorId) {
        return Service.builder().id(SERVICE_ID).uuid(SERVICE_UUID).vendorId(vendorId).name("Netflix").build();
    }

    private Profile buildProfile(Long vendorId, boolean owner) {
        return Profile.builder()
                .id(PROFILE_ID)
                .uuid(PROFILE_UUID)
                .vendorId(vendorId)
                .accountId(ACCOUNT_ID)
                .isOwner(owner)
                .status(ProfileStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create BY_PROFILE subscription and optionally deliver access")
        void create_byProfile_shouldCreateAndDeliverAccess() {
            Subscription input = buildInput();
            Client client = buildClient(VENDOR_ID);
            Service service = buildService(VENDOR_ID);
            Profile profile = buildProfile(VENDOR_ID, false);
            Account account = Account.builder()
                    .id(ACCOUNT_ID).uuid(UUID.randomUUID()).serviceId(SERVICE_ID)
                    .saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();

            mockOwnershipResolution();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(client));
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(profile));
            when(subscriptionRepositoryPort.existsActiveByProfileId(PROFILE_ID)).thenReturn(false);
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> {
                Subscription saved = inv.getArgument(0);
                saved.setUuid(UUID.randomUUID());
                return saved;
            });

            Subscription result = createManualSubscriptionService.create(input, true, EXTERNAL_ID);

            assertThat(result.getStatus()).isEqualTo(SubStatus.ACTIVE);
            assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 4, 29));
            assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 5, 29));
            assertThat(result.getServiceId()).isEqualTo(SERVICE_ID);
            assertThat(result.getPriceSold()).isEqualByComparingTo("100.00");
            assertThat(profile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
            verify(profileRepositoryPort).save(profile);
            verify(deliverAccessUseCase).deliver(result);
        }

        @Test
        @DisplayName("should create FULL_ACCOUNT subscription and activate all profiles")
        void create_fullAccount_shouldActivateProfilesAndAccount() {
            Subscription input = buildInput();
            Client client = buildClient(VENDOR_ID);
            Service service = buildService(VENDOR_ID);
            Profile ownerProfile = buildProfile(VENDOR_ID, true);
            Profile secondaryProfile = Profile.builder()
                    .id(31L).uuid(UUID.randomUUID()).vendorId(VENDOR_ID)
                    .accountId(ACCOUNT_ID).status(ProfileStatus.AVAILABLE).build();
            Account account = Account.builder()
                    .id(ACCOUNT_ID).uuid(UUID.randomUUID()).serviceId(SERVICE_ID)
                    .saleMode(SaleMode.FULL_ACCOUNT).status(AccountStatus.AVAILABLE).build();

            mockOwnershipResolution();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(client));
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(ownerProfile));
            when(subscriptionRepositoryPort.existsActiveByProfileId(PROFILE_ID)).thenReturn(false);
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(profileRepositoryPort.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(ownerProfile, secondaryProfile));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = createManualSubscriptionService.create(input, false, EXTERNAL_ID);

            assertThat(result.getStatus()).isEqualTo(SubStatus.ACTIVE);
            assertThat(ownerProfile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
            assertThat(secondaryProfile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
            assertThat(account.getStatus()).isEqualTo(AccountStatus.FULL);
            verify(profileRepositoryPort).saveAll(List.of(ownerProfile, secondaryProfile));
            verify(accountRepositoryPort).save(account);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when client belongs to another vendor")
        void create_notOwnedClient_shouldThrow403() {
            mockOwnershipResolution();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient(99L)));

            assertThatThrownBy(() -> createManualSubscriptionService.create(buildInput(), false, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should throw BusinessRuleException when profile already has active subscription")
        void create_profileAlreadyActive_shouldThrowOverbooking() {
            mockOwnershipResolution();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient(VENDOR_ID)));
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(buildService(VENDOR_ID)));
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(buildProfile(VENDOR_ID, false)));
            when(subscriptionRepositoryPort.existsActiveByProfileId(PROFILE_ID)).thenReturn(true);

            assertThatThrownBy(() -> createManualSubscriptionService.create(buildInput(), false, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("active subscription");
        }

        @Test
        @DisplayName("should create subscription with custom startDate when provided")
        void create_withCustomStartDate_shouldPreserveStartDate() {
            LocalDate customStart = LocalDate.of(2026, 4, 15);
            Subscription input = Subscription.builder()
                    .clientUuid(CLIENT_UUID)
                    .profileUuid(PROFILE_UUID)
                    .serviceUuid(SERVICE_UUID)
                    .startDate(customStart)
                    .paymentDueDate(LocalDate.of(2026, 5, 15))
                    .priceSold(new BigDecimal("100.00"))
                    .discountApplied(new BigDecimal("5.00"))
                    .notes("External sale")
                    .build();

            Client client = buildClient(VENDOR_ID);
            Service service = buildService(VENDOR_ID);
            Profile profile = buildProfile(VENDOR_ID, false);
            Account account = Account.builder()
                    .id(ACCOUNT_ID).uuid(UUID.randomUUID()).serviceId(SERVICE_ID)
                    .saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();

            mockOwnershipResolution();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(client));
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(profile));
            when(subscriptionRepositoryPort.existsActiveByProfileId(PROFILE_ID)).thenReturn(false);
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = createManualSubscriptionService.create(input, false, EXTERNAL_ID);

            assertThat(result.getStartDate()).isEqualTo(customStart);
            assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        }

        @Test
        @DisplayName("should throw BadRequestException when paymentDueDate is before custom startDate")
        void create_paymentDueDateBeforeCustomStartDate_shouldThrow400() {
            LocalDate customStart = LocalDate.of(2026, 4, 15);
            Subscription input = Subscription.builder()
                    .clientUuid(CLIENT_UUID)
                    .profileUuid(PROFILE_UUID)
                    .serviceUuid(SERVICE_UUID)
                    .startDate(customStart)
                    .paymentDueDate(LocalDate.of(2026, 4, 10))
                    .priceSold(new BigDecimal("100.00"))
                    .discountApplied(new BigDecimal("5.00"))
                    .build();

            Client client = buildClient(VENDOR_ID);
            Service service = buildService(VENDOR_ID);
            Profile profile = buildProfile(VENDOR_ID, false);
            Account account = Account.builder()
                    .id(ACCOUNT_ID).uuid(UUID.randomUUID()).serviceId(SERVICE_ID)
                    .saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();

            mockOwnershipResolution();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(client));
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(profile));
            when(subscriptionRepositoryPort.existsActiveByProfileId(PROFILE_ID)).thenReturn(false);
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> createManualSubscriptionService.create(input, false, EXTERNAL_ID))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Payment due date must be on or after start date");
        }
    }
}
