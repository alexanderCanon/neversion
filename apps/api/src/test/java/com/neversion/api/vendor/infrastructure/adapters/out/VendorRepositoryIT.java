package com.neversion.api.vendor.infrastructure.adapters.out;

import com.neversion.api.BaseIntegrationTest;
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
@DisplayName("VendorRepositoryPort integration tests")
class VendorRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private VendorRepositoryPort vendorRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    private User vendorUser;

    @BeforeEach
    void setUp() {
        vendorUser = userRepositoryPort.save(
                User.builder()
                        .externalId("auth|vendor-" + System.nanoTime())
                        .role(UserRole.VENDOR)
                        .build());
    }

    private Vendor buildVendor(Long userId, String storeName) {
        return Vendor.builder()
                .userId(userId)
                .storeName(storeName)
                .logoUrl("https://cdn.example.com/logo.png")
                .bankDetails("{\"bank\":\"Banrural\",\"account\":\"123456\"}")
                .discountCfg("{\"min_items\":2,\"tiers\":[{\"from\":2,\"to\":3,\"discount_pct\":5}]}")
                .build();
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save - should persist vendor with all fields and generate uuid")
    void save_shouldPersistVendor_withAllFields() {
        Vendor saved = vendorRepositoryPort.save(buildVendor(vendorUser.getId(), "Mi Tienda"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(vendorUser.getId());
        assertThat(saved.getStoreName()).isEqualTo("Mi Tienda");
        assertThat(saved.getLogoUrl()).isEqualTo("https://cdn.example.com/logo.png");
        assertThat(saved.getBankDetails()).contains("Banrural");
        assertThat(saved.getDiscountCfg()).contains("min_items");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ─── findByUuid ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUuid - should return vendor when found")
    void findByUuid_shouldReturnVendor_whenFound() {
        Vendor saved = vendorRepositoryPort.save(buildVendor(vendorUser.getId(), "Tienda UUID"));

        Optional<Vendor> found = vendorRepositoryPort.findByUuid(saved.getUuid());

        assertThat(found).isPresent();
        assertThat(found.get().getStoreName()).isEqualTo("Tienda UUID");
    }

    @Test
    @DisplayName("findByUuid - should return empty when not found")
    void findByUuid_shouldReturnEmpty_whenNotFound() {
        Optional<Vendor> found = vendorRepositoryPort.findByUuid(java.util.UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    // ─── findByUserId ────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId - should return vendor linked to user")
    void findByUserId_shouldReturnVendor_linkedToUser() {
        vendorRepositoryPort.save(buildVendor(vendorUser.getId(), "Tienda Usuario"));

        Optional<Vendor> found = vendorRepositoryPort.findByUserId(vendorUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(vendorUser.getId());
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll - should return all persisted vendors")
    void findAll_shouldReturnAllVendors() {
        // Create a second user for the second vendor (unique user_id constraint)
        User secondUser = userRepositoryPort.save(
                User.builder()
                        .externalId("auth|vendor2-" + System.nanoTime())
                        .role(UserRole.VENDOR)
                        .build());

        vendorRepositoryPort.save(buildVendor(vendorUser.getId(), "Tienda A"));
        vendorRepositoryPort.save(buildVendor(secondUser.getId(), "Tienda B"));

        List<Vendor> vendors = vendorRepositoryPort.findAll();

        assertThat(vendors).hasSizeGreaterThanOrEqualTo(2);
    }

    // ─── existsByUserId ──────────────────────────────────────────────────────

    @Test
    @DisplayName("existsByUserId - should return true when vendor exists for user")
    void existsByUserId_shouldReturnTrue_whenExists() {
        vendorRepositoryPort.save(buildVendor(vendorUser.getId(), "Tienda Exists"));

        assertThat(vendorRepositoryPort.existsByUserId(vendorUser.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByUserId - should return false when no vendor for user")
    void existsByUserId_shouldReturnFalse_whenNotExists() {
        assertThat(vendorRepositoryPort.existsByUserId(999999L)).isFalse();
    }

    // ─── deleteByUuid ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteByUuid - should remove vendor")
    void deleteByUuid_shouldRemoveVendor() {
        Vendor saved = vendorRepositoryPort.save(buildVendor(vendorUser.getId(), "Tienda Delete"));

        vendorRepositoryPort.deleteByUuid(saved.getUuid());

        Optional<Vendor> found = vendorRepositoryPort.findByUuid(saved.getUuid());
        assertThat(found).isEmpty();
    }
}
