package com.neversion.api.service.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@SpringBootTest
@Transactional
@DisplayName("ServiceRepositoryPort integration tests")
class ServiceRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private ServiceRepositoryPort serviceRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private VendorRepositoryPort vendorRepositoryPort;

    private Long createVendorId() {
        User vendorUser = userRepositoryPort.save(
                User.builder()
                        .externalId("auth|service-vendor-" + System.nanoTime())
                        .role(UserRole.VENDOR)
                        .build());

        Vendor vendor = vendorRepositoryPort.save(
                Vendor.builder()
                        .userId(vendorUser.getId())
                        .storeName("Service Vendor " + System.nanoTime())
                        .build());

        return vendor.getId();
    }

    private Service buildService(String name) {
        return buildService(name, null, CategoryType.STREAMING, true);
    }

    private Service buildService(String name, Long vendorId, CategoryType category, Boolean isActive) {
        return Service.builder()
                .name(name)
                .vendorId(vendorId)
                .maxProfiles(5)
                .details(null)
                .category(category)
                .isActive(isActive)
                .build();
    }

    @Test
    @DisplayName("save - should persist service and assign uuid")
    void save_shouldPersistService_andAssignUuid() {
        // Given
        Service service = buildService("Netflix");

        // When
        Service saved = serviceRepositoryPort.save(service);

        // Then
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Netflix");
        assertThat(saved.getMaxProfiles()).isEqualTo(5);
    }

    @Test
    @DisplayName("findById - should return service by uuid")
    void findById_shouldReturnService_byUuid() {
        // Given
        Service saved = serviceRepositoryPort.save(buildService("Disney+"));

        // When
        Optional<Service> found = serviceRepositoryPort.findById(saved.getUuid());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Disney+");
        assertThat(found.get().getMaxProfiles()).isEqualTo(5);
    }

    @Test
    @DisplayName("findByName - should return service when exists")
    void findByName_shouldReturnService_whenExists() {
        // Given
        serviceRepositoryPort.save(buildService("Spotify"));

        // When
        Optional<Service> found = serviceRepositoryPort.findByName("Spotify");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Spotify");
    }

    @Test
    @DisplayName("existsByName - should return true when name exists (BR-17)")
    void existsByName_shouldReturnTrue_whenNameExists() {
        // Given
        serviceRepositoryPort.save(buildService("HBO Max"));

        // When
        boolean exists = serviceRepositoryPort.existsByName("HBO Max");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByName - should return false when name not exists (BR-17)")
    void existsByName_shouldReturnFalse_whenNameNotExists() {
        // When
        boolean exists = serviceRepositoryPort.existsByName("NonExistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByVendorIdAndFilters - should filter by active status when category is null")
    void findByVendorIdAndFilters_nullCategory_shouldFilterByActiveStatus() {
        Long vendorId = createVendorId();
        Service active = serviceRepositoryPort.save(
                buildService("Active Service " + vendorId, vendorId, CategoryType.STREAMING, true));
        serviceRepositoryPort.save(
                buildService("Inactive Service " + vendorId, vendorId, CategoryType.DIGITAL_SERVICE, false));

        List<Service> result = serviceRepositoryPort.findByVendorIdAndFilters(vendorId, null, true);

        assertThat(result)
                .extracting(Service::getUuid)
                .containsExactly(active.getUuid());
    }

    @Test
    @DisplayName("findByVendorIdAndFilters - should return vendor services when optional filters are null")
    void findByVendorIdAndFilters_nullFilters_shouldReturnVendorServices() {
        Long vendorId = createVendorId();
        Service first = serviceRepositoryPort.save(
                buildService("Vendor Service A " + vendorId, vendorId, CategoryType.STREAMING, true));
        Service second = serviceRepositoryPort.save(
                buildService("Vendor Service B " + vendorId, vendorId, CategoryType.DIGITAL_SERVICE, false));

        List<Service> result = serviceRepositoryPort.findByVendorIdAndFilters(vendorId, null, null);

        assertThat(result)
                .extracting(Service::getUuid)
                .contains(first.getUuid(), second.getUuid());
    }

    @Test
    @DisplayName("save - should throw DataIntegrityViolation when name duplicated")
    void save_shouldThrowDataIntegrityViolation_whenNameDuplicated() {
        // Given
        serviceRepositoryPort.save(buildService("Amazon Prime"));

        // When / Then
        assertThatThrownBy(() -> serviceRepositoryPort.save(buildService("Amazon Prime")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("deleteById - should remove service")
    void deleteById_shouldRemoveService() {
        // Given
        Service saved = serviceRepositoryPort.save(buildService("Crunchyroll"));

        // When
        serviceRepositoryPort.deleteById(saved.getUuid());

        // Then
        Optional<Service> found = serviceRepositoryPort.findById(saved.getUuid());
        assertThat(found).isEmpty();
    }
}
