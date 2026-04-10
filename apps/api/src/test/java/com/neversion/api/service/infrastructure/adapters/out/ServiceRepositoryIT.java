package com.neversion.api.service.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

@SpringBootTest
@Transactional
@DisplayName("ServiceRepositoryPort integration tests")
class ServiceRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private ServiceRepositoryPort serviceRepositoryPort;

    private Service buildService(String name) {
        return Service.builder()
                .name(name)
                .maxProfiles(5)
                .details(null)
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
