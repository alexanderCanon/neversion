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
    @DisplayName("save - should persist user and generate id")
    void save_shouldPersistUser_withGeneratedId() {
        User user = buildUser("auth|abc123", UserRole.VENDOR);

        User saved = userRepositoryPort.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getExternalId()).isEqualTo("auth|abc123");
        assertThat(saved.getRole()).isEqualTo(UserRole.VENDOR);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("save - should persist role as lowercase in DB")
    void save_shouldPersistRole_asLowercase() {
        User saved = userRepositoryPort.save(buildUser("auth|super1", UserRole.SUPER_ADMIN));

        // Re-fetch to confirm DB round-trip
        Optional<User> found = userRepositoryPort.findByExternalId(saved.getExternalId());

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(UserRole.SUPER_ADMIN);
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
}

