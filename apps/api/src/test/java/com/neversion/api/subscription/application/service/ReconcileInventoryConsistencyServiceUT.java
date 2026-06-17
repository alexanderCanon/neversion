package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReconcileInventoryConsistencyService Unit Tests")
class ReconcileInventoryConsistencyServiceUT {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock
    private ProfileRepositoryPort profileRepositoryPort;

    @InjectMocks
    private ReconcileInventoryConsistencyService service;

    private Subscription subscription(Long profileId, SubStatus status) {
        return Subscription.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .profileId(profileId)
                .status(status)
                .build();
    }

    private Profile profile(Long id, ProfileStatus status) {
        return Profile.builder()
                .id(id)
                .uuid(UUID.randomUUID())
                .status(status)
                .build();
    }

    @Test
    @DisplayName("detectInconsistencies - should report zero when all profiles match expected state")
    void detectInconsistencies_allConsistent_shouldReturnZero() {
        when(subscriptionRepositoryPort.findByStatus(SubStatus.ACTIVE))
                .thenReturn(List.of(subscription(10L, SubStatus.ACTIVE)));
        when(subscriptionRepositoryPort.findByStatus(SubStatus.SUSPENDED))
                .thenReturn(List.of(subscription(20L, SubStatus.SUSPENDED)));
        when(profileRepositoryPort.findByInternalId(10L))
                .thenReturn(Optional.of(profile(10L, ProfileStatus.ACTIVE)));
        when(profileRepositoryPort.findByInternalId(20L))
                .thenReturn(Optional.of(profile(20L, ProfileStatus.RESERVED)));

        int result = service.detectInconsistencies();

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("detectInconsistencies - should flag ACTIVE subscription whose profile is AVAILABLE")
    void detectInconsistencies_activeWithAvailableProfile_shouldFlag() {
        when(subscriptionRepositoryPort.findByStatus(SubStatus.ACTIVE))
                .thenReturn(List.of(subscription(10L, SubStatus.ACTIVE)));
        when(subscriptionRepositoryPort.findByStatus(SubStatus.SUSPENDED))
                .thenReturn(List.of());
        when(profileRepositoryPort.findByInternalId(10L))
                .thenReturn(Optional.of(profile(10L, ProfileStatus.AVAILABLE)));

        int result = service.detectInconsistencies();

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("detectInconsistencies - should accept EXPIRED profile for SUSPENDED subscription")
    void detectInconsistencies_suspendedWithExpiredProfile_shouldNotFlag() {
        when(subscriptionRepositoryPort.findByStatus(SubStatus.ACTIVE))
                .thenReturn(List.of());
        when(subscriptionRepositoryPort.findByStatus(SubStatus.SUSPENDED))
                .thenReturn(List.of(subscription(20L, SubStatus.SUSPENDED)));
        when(profileRepositoryPort.findByInternalId(20L))
                .thenReturn(Optional.of(profile(20L, ProfileStatus.EXPIRED)));

        int result = service.detectInconsistencies();

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("detectInconsistencies - should flag SUSPENDED subscription whose profile is still ACTIVE")
    void detectInconsistencies_suspendedWithActiveProfile_shouldFlag() {
        when(subscriptionRepositoryPort.findByStatus(SubStatus.ACTIVE))
                .thenReturn(List.of());
        when(subscriptionRepositoryPort.findByStatus(SubStatus.SUSPENDED))
                .thenReturn(List.of(subscription(20L, SubStatus.SUSPENDED)));
        when(profileRepositoryPort.findByInternalId(20L))
                .thenReturn(Optional.of(profile(20L, ProfileStatus.ACTIVE)));

        int result = service.detectInconsistencies();

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("detectInconsistencies - should flag subscription referencing a missing profile")
    void detectInconsistencies_missingProfile_shouldFlag() {
        when(subscriptionRepositoryPort.findByStatus(SubStatus.ACTIVE))
                .thenReturn(List.of(subscription(99L, SubStatus.ACTIVE)));
        when(subscriptionRepositoryPort.findByStatus(SubStatus.SUSPENDED))
                .thenReturn(List.of());
        when(profileRepositoryPort.findByInternalId(99L))
                .thenReturn(Optional.empty());

        int result = service.detectInconsistencies();

        assertThat(result).isEqualTo(1);
    }
}
