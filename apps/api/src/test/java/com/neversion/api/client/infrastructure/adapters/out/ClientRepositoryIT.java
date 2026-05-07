package com.neversion.api.client.infrastructure.adapters.out;

import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("ClientRepositoryPort integration tests — US-003")
class ClientRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private ClientRepositoryPort clientRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private VendorRepositoryPort vendorRepositoryPort;

    private User clientUser;
    private Vendor parentVendor;

    @BeforeEach
    void setUp() {
        // Create a vendor user + vendor for FK references
        User vendorUser = userRepositoryPort.save(
                User.builder()
                        .externalId("auth|vendorForClient-" + System.nanoTime())
                        .role(UserRole.VENDOR)
                        .build());
        parentVendor = vendorRepositoryPort.save(
                Vendor.builder()
                        .userId(vendorUser.getId())
                        .storeName("Test Vendor " + System.nanoTime())
                        .build());

        // Create a client user
        clientUser = userRepositoryPort.save(
                User.builder()
                        .externalId("auth|client-" + System.nanoTime())
                        .role(UserRole.CLIENT)
                        .build());
    }

    private Client buildClient(String name) {
        return Client.builder()
                .userId(clientUser.getId())
                .vendorId(parentVendor.getId())
                .name(name)
                .phone("55551234")
                .email(name.toLowerCase().replace(" ", "") + "@test.com")
                .notes("Test client")
                .build();
    }

    // ─── save with new FK fields ─────────────────────────────────────────

    @Test
    @DisplayName("save - should persist client with userId and vendorId")
    void save_shouldPersistClient_withUserIdAndVendorId() {
        Client saved = clientRepositoryPort.save(buildClient("Juan Pérez"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(clientUser.getId());
        assertThat(saved.getVendorId()).isEqualTo(parentVendor.getId());
        assertThat(saved.getName()).isEqualTo("Juan Pérez");
    }

    // ─── save without FKs (nullable — backward compat) ──────────────────

    @Test
    @DisplayName("save - should persist client without userId and vendorId (nullable)")
    void save_shouldPersistClient_withoutFks() {
        Client client = Client.builder()
                .name("Legacy Client")
                .phone("55559999")
                .build();

        Client saved = clientRepositoryPort.save(client);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isNull();
        assertThat(saved.getVendorId()).isNull();
    }

    // ─── findById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById - should return client with FK fields populated")
    void findById_shouldReturnClient_withFkFields() {
        Client saved = clientRepositoryPort.save(buildClient("Find Test"));

        Optional<Client> found = clientRepositoryPort.findById(saved.getUuid());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(clientUser.getId());
        assertThat(found.get().getVendorId()).isEqualTo(parentVendor.getId());
    }

    // ─── findByVendorId with optional filters ────────────────────────────

    @Test
    @DisplayName("findByVendorId - should return vendor clients when optional filters are null")
    void findByVendorId_nullFilters_shouldReturnVendorClients() {
        Client first = clientRepositoryPort.save(buildClient("Ana Gómez"));
        Client second = clientRepositoryPort.save(buildClient("Carlos Ruiz"));

        List<Client> result = clientRepositoryPort.findByVendorId(
                parentVendor.getId(), null, null, null);

        assertThat(result)
                .extracting(Client::getUuid)
                .contains(first.getUuid(), second.getUuid());
    }

    @Test
    @DisplayName("findByVendorId - should apply text filters only when provided")
    void findByVendorId_textFilters_shouldApplyProvidedFilters() {
        Client expected = clientRepositoryPort.save(buildClient("Maria Lopez"));
        clientRepositoryPort.save(buildClient("Carlos Ruiz"));

        List<Client> result = clientRepositoryPort.findByVendorId(
                parentVendor.getId(), "maria", "5555", "MARIALOPEZ");

        assertThat(result)
                .extracting(Client::getUuid)
                .containsExactly(expected.getUuid());
    }

    // ─── deleteById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById - should remove client")
    void deleteById_shouldRemoveClient() {
        Client saved = clientRepositoryPort.save(buildClient("Delete Test"));

        clientRepositoryPort.deleteById(saved.getUuid());

        Optional<Client> found = clientRepositoryPort.findById(saved.getUuid());
        assertThat(found).isEmpty();
    }
}
