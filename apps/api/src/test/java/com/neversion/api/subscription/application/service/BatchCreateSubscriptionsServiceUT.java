package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.subscription.application.port.in.BatchCreateSubscriptionsUseCase.BatchCommand;
import com.neversion.api.subscription.application.port.in.BatchCreateSubscriptionsUseCase.BatchItemCommand;
import com.neversion.api.subscription.application.port.in.BatchCreateSubscriptionsUseCase.BatchItemResult;
import com.neversion.api.subscription.application.port.in.BatchCreateSubscriptionsUseCase.BatchResult;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchCreateSubscriptionsService — unit tests")
class BatchCreateSubscriptionsServiceUT {

    @Mock private CreateManualSubscriptionUseCase createManualSubscriptionUseCase;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    private BatchCreateSubscriptionsService batchService;

    private static final String EXTERNAL_ID = "auth|batch";
    private static final Long USER_ID = 5L;
    private static final Long VENDOR_ID = 10L;
    private static final Long SERVICE_ID = 50L;
    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final UUID SERVICE_UUID = UUID.randomUUID();
    private static final UUID PROFILE_1_UUID = UUID.randomUUID();
    private static final UUID PROFILE_2_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        batchService = new BatchCreateSubscriptionsService(
                createManualSubscriptionUseCase,
                profileRepositoryPort,
                serviceRepositoryPort,
                new VendorSecurityService(userRepositoryPort, vendorRepositoryPort));
    }

    private void mockOwnershipResolution() {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).role(UserRole.VENDOR).build();
        Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).storeName("Vendor").build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private Subscription mockCreatedSubscription(UUID profileUuid) {
        return Subscription.builder()
                .uuid(UUID.randomUUID())
                .clientUuid(CLIENT_UUID)
                .profileUuid(profileUuid)
                .serviceUuid(SERVICE_UUID)
                .status(SubStatus.ACTIVE)
                .build();
    }

    private Profile mockProfile(UUID uuid) {
        return Profile.builder()
                .id(Long.valueOf(uuid.hashCode() & 0xFFFFFF))
                .uuid(uuid)
                .status(ProfileStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should auto-assign profiles and create subscriptions for multiple items")
        void create_autoAssign_shouldCreateAll() {
            mockOwnershipResolution();
            Service service = Service.builder().id(SERVICE_ID).uuid(SERVICE_UUID).vendorId(VENDOR_ID).build();
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                    .thenReturn(List.of(mockProfile(PROFILE_1_UUID), mockProfile(PROFILE_2_UUID)));

            when(createManualSubscriptionUseCase.create(any(Subscription.class), anyBoolean(), anyString()))
                    .thenAnswer(inv -> mockCreatedSubscription(inv.getArgument(0, Subscription.class).getProfileUuid()));

            BatchCommand command = new BatchCommand(
                    CLIENT_UUID,
                    List.of(new BatchItemCommand(SERVICE_UUID, 2, new BigDecimal("100.00"), null)),
                    new BigDecimal("10.00"),
                    LocalDate.of(2026, 7, 1),
                    "Batch notes",
                    true);

            BatchResult result = batchService.create(command, EXTERNAL_ID);

            assertThat(result.totalRequested()).isEqualTo(2);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failedCount()).isEqualTo(0);
            assertThat(result.results()).hasSize(2);
            assertThat(result.results().get(0).success()).isTrue();
            assertThat(result.results().get(1).success()).isTrue();
            verify(createManualSubscriptionUseCase, times(2)).create(any(Subscription.class), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("should use manual profileId when provided")
        void create_manualOverride_shouldUseProvidedProfile() {
            mockOwnershipResolution();

            when(createManualSubscriptionUseCase.create(any(Subscription.class), anyBoolean(), anyString()))
                    .thenAnswer(inv -> mockCreatedSubscription(inv.getArgument(0, Subscription.class).getProfileUuid()));

            BatchCommand command = new BatchCommand(
                    CLIENT_UUID,
                    List.of(new BatchItemCommand(SERVICE_UUID, 1, new BigDecimal("80.00"), PROFILE_1_UUID)),
                    BigDecimal.ZERO,
                    LocalDate.of(2026, 7, 1),
                    null,
                    false);

            BatchResult result = batchService.create(command, EXTERNAL_ID);

            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.results().get(0).success()).isTrue();
            assertThat(result.results().get(0).subscriptionUuid()).isNotNull();
            verify(profileRepositoryPort, never()).findAvailableByServiceIdAndVendorId(any(), any());
        }

        @Test
        @DisplayName("should report failure when no available profiles for auto-assign")
        void create_autoAssignNoProfiles_shouldReportFailure() {
            mockOwnershipResolution();
            Service service = Service.builder().id(SERVICE_ID).uuid(SERVICE_UUID).vendorId(VENDOR_ID).build();
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                    .thenReturn(List.of());

            BatchCommand command = new BatchCommand(
                    CLIENT_UUID,
                    List.of(new BatchItemCommand(SERVICE_UUID, 1, new BigDecimal("50.00"), null)),
                    BigDecimal.ZERO,
                    LocalDate.of(2026, 7, 1),
                    null,
                    false);

            BatchResult result = batchService.create(command, EXTERNAL_ID);

            assertThat(result.successCount()).isEqualTo(0);
            assertThat(result.failedCount()).isEqualTo(1);
            assertThat(result.results().get(0).success()).isFalse();
            assertThat(result.results().get(0).errorMessage()).contains("perfiles disponibles");
            verify(createManualSubscriptionUseCase, never()).create(any(), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("should report failure when create throws, without stopping remaining items")
        void create_partialFailure_shouldContinueRemainingItems() {
            mockOwnershipResolution();
            Service service = Service.builder().id(SERVICE_ID).uuid(SERVICE_UUID).vendorId(VENDOR_ID).build();
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                    .thenReturn(List.of(mockProfile(PROFILE_1_UUID), mockProfile(PROFILE_2_UUID)));

            when(createManualSubscriptionUseCase.create(any(Subscription.class), anyBoolean(), anyString()))
                    .thenThrow(new RuntimeException("Profile already assigned"))
                    .thenAnswer(inv -> mockCreatedSubscription(inv.getArgument(0, Subscription.class).getProfileUuid()));

            BatchCommand command = new BatchCommand(
                    CLIENT_UUID,
                    List.of(new BatchItemCommand(SERVICE_UUID, 2, new BigDecimal("100.00"), null)),
                    BigDecimal.ZERO,
                    LocalDate.of(2026, 7, 1),
                    null,
                    false);

            BatchResult result = batchService.create(command, EXTERNAL_ID);

            assertThat(result.totalRequested()).isEqualTo(2);
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.failedCount()).isEqualTo(1);
            assertThat(result.results().get(0).success()).isFalse();
            assertThat(result.results().get(0).errorMessage()).contains("Profile already assigned");
            assertThat(result.results().get(1).success()).isTrue();
        }

        @Test
        @DisplayName("should not assign same profile twice within a single batch")
        void create_autoAssign_shouldNotDuplicateProfileInSameBatch() {
            mockOwnershipResolution();
            Service service = Service.builder().id(SERVICE_ID).uuid(SERVICE_UUID).vendorId(VENDOR_ID).build();
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(profileRepositoryPort.findAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                    .thenReturn(List.of(mockProfile(PROFILE_1_UUID), mockProfile(PROFILE_2_UUID)));

            when(createManualSubscriptionUseCase.create(any(Subscription.class), anyBoolean(), anyString()))
                    .thenAnswer(inv -> mockCreatedSubscription(inv.getArgument(0, Subscription.class).getProfileUuid()));

            BatchCommand command = new BatchCommand(
                    CLIENT_UUID,
                    List.of(
                            new BatchItemCommand(SERVICE_UUID, 1, new BigDecimal("100.00"), null),
                            new BatchItemCommand(SERVICE_UUID, 1, new BigDecimal("90.00"), null)),
                    BigDecimal.ZERO,
                    LocalDate.of(2026, 7, 1),
                    null,
                    false);

            BatchResult result = batchService.create(command, EXTERNAL_ID);

            assertThat(result.successCount()).isEqualTo(2);
            BatchItemResult first = result.results().get(0);
            BatchItemResult second = result.results().get(1);
            assertThat(first.subscriptionUuid()).isNotEqualTo(second.subscriptionUuid());
        }
    }
}
