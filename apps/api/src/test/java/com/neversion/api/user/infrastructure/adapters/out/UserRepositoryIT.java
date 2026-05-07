package com.neversion.api.user.infrastructure.adapters.out;

import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("UserRepositoryPort integration tests")
class UserRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    private User buildUser(String externalId, UserRole role) {
        return User.builder()
                .externalId(externalId)
                .role(role)
                .build();
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save - should persist user and generate uuid and id")
    void save_shouldPersistUser_withGeneratedUuidAndId() {
        User user = buildUser("auth|abc123", UserRole.VENDOR);

        User saved = userRepositoryPort.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getExternalId()).isEqualTo("auth|abc123");
        assertThat(saved.getRole()).isEqualTo(UserRole.VENDOR);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save - should persist role as lowercase in DB")
    void save_shouldPersistRole_asLowercase() {
        User saved = userRepositoryPort.save(buildUser("auth|super1", UserRole.SUPER_ADMIN));

        // Re-fetch to confirm DB round-trip
        Optional<User> found = userRepositoryPort.findByUuid(saved.getUuid());

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(UserRole.SUPER_ADMIN);
    }

    // ─── findByUuid ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUuid - should return user when found")
    void findByUuid_shouldReturnUser_whenFound() {
        User saved = userRepositoryPort.save(buildUser("auth|find1", UserRole.CLIENT));

        Optional<User> found = userRepositoryPort.findByUuid(saved.getUuid());

        assertThat(found).isPresent();
        assertThat(found.get().getExternalId()).isEqualTo("auth|find1");
    }

    @Test
    @DisplayName("findByUuid - should return empty when not found")
    void findByUuid_shouldReturnEmpty_whenNotFound() {
        Optional<User> found = userRepositoryPort.findByUuid(java.util.UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    // ─── findByExternalId ────────────────────────────────────────────────────

    @Test
    @DisplayName("findByExternalId - should return user when found")
    void findByExternalId_shouldReturnUser_whenFound() {
        userRepositoryPort.save(buildUser("auth|extid1", UserRole.VENDOR));

        Optional<User> found = userRepositoryPort.findByExternalId("auth|extid1");

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(UserRole.VENDOR);
    }

    // ─── existsByExternalId ──────────────────────────────────────────────────

    @Test
    @DisplayName("existsByExternalId - should return true when exists")
    void existsByExternalId_shouldReturnTrue_whenExists() {
        userRepositoryPort.save(buildUser("auth|exists1", UserRole.CLIENT));

        assertThat(userRepositoryPort.existsByExternalId("auth|exists1")).isTrue();
    }

    @Test
    @DisplayName("existsByExternalId - should return false when not exists")
    void existsByExternalId_shouldReturnFalse_whenNotExists() {
        assertThat(userRepositoryPort.existsByExternalId("auth|nonexistent")).isFalse();
    }

    // ─── deleteByUuid ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteByUuid - should remove user")
    void deleteByUuid_shouldRemoveUser() {
        User saved = userRepositoryPort.save(buildUser("auth|delete1", UserRole.CLIENT));

        userRepositoryPort.deleteByUuid(saved.getUuid());

        Optional<User> found = userRepositoryPort.findByUuid(saved.getUuid());
        assertThat(found).isEmpty();
    }
}
