package com.neversion.api.game.application.service;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.domain.port.out.GameRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameService unit tests")
class GameServiceUT {

    @Mock
    private GameRepositoryPort gameRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private VendorRepositoryPort vendorRepositoryPort;

    private GameService sut;

    private static final String EXTERNAL_ID = "supabase-user-uuid-123";
    private static final Long USER_ID = 10L;
    private static final Long VENDOR_ID = 20L;
    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final UUID GAME_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sut = new GameService(gameRepositoryPort, userRepositoryPort, vendorRepositoryPort);
    }

    private void stubCallerChain(String extId, Long userId, Long vendorId) {
        User user = User.builder().id(userId).externalId(extId).build();

        Vendor vendor = Vendor.builder().id(vendorId).uuid(VENDOR_UUID).userId(userId).build();

        when(userRepositoryPort.findByExternalId(extId)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(vendor));
    }

    private Game buildGameInput() {
        return Game.builder()
                .name("Free Fire")
                .slug("free-fire")
                .imageUrl("https://image.url/ff.png")
                .build();
    }

    private Game buildSavedGame(Long vendorId) {
        return Game.builder()
                .id(1L)
                .uuid(GAME_UUID)
                .vendorId(vendorId)
                .name("Free Fire")
                .slug("free-fire")
                .imageUrl("https://image.url/ff.png")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Create")
    class CreateTests {

        @Test
        @DisplayName("should create game with vendorId resolved from JWT subject")
        void create_shouldResolveVendorAndSave() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Game input = buildGameInput();
            Game saved = buildSavedGame(VENDOR_ID);

            when(gameRepositoryPort.existsByVendorIdAndSlug(VENDOR_ID, "free-fire")).thenReturn(false);
            when(gameRepositoryPort.save(any())).thenReturn(saved);

            Game result = sut.create(input, EXTERNAL_ID);

            assertThat(result.getVendorId()).isEqualTo(VENDOR_ID);
            assertThat(result.getIsActive()).isTrue();
            verify(gameRepositoryPort).save(any());
        }

        @Test
        @DisplayName("should throw BusinessRuleException when slug already exists for vendor")
        void create_shouldThrow_whenDuplicateSlug() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Game input = buildGameInput();

            when(gameRepositoryPort.existsByVendorIdAndSlug(VENDOR_ID, "free-fire")).thenReturn(true);

            assertThatThrownBy(() -> sut.create(input, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("free-fire");

            verify(gameRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update")
    class UpdateTests {

        @Test
        @DisplayName("should update game when caller owns it")
        void update_shouldSave_whenCallerIsOwner() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Game existing = buildSavedGame(VENDOR_ID);
            Game updatedInput = Game.builder()
                    .name("Free Fire Updated")
                    .slug("free-fire-v2")
                    .imageUrl("https://new.url")
                    .build();

            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(existing));
            when(gameRepositoryPort.existsByVendorIdAndSlug(VENDOR_ID, "free-fire-v2")).thenReturn(false);
            when(gameRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Game result = sut.update(GAME_UUID, updatedInput, EXTERNAL_ID);

            assertThat(result.getSlug()).isEqualTo("free-fire-v2");
            assertThat(result.getName()).isEqualTo("Free Fire Updated");
            assertThat(result.getImageUrl()).isEqualTo("https://new.url");
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own the game")
        void update_shouldThrowAccessDenied_whenNotOwner() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Game existing = buildSavedGame(999L); // owned by different vendor
            Game updatedInput = buildGameInput();

            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> sut.update(GAME_UUID, updatedInput, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(gameRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Toggle Status")
    class ToggleStatusTests {

        @Test
        @DisplayName("should toggle active status")
        void toggle_shouldInvertIsActive() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Game existing = buildSavedGame(VENDOR_ID);
            assertThat(existing.getIsActive()).isTrue();

            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(existing));
            when(gameRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Game result = sut.toggleStatus(GAME_UUID, EXTERNAL_ID);

            assertThat(result.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Get Active By Slug")
    class GetActiveBySlugTests {

        @Test
        @DisplayName("should return game when slug exists and game is active")
        void getActiveBySlug_shouldReturnGame_whenActive() {
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            Game game = buildSavedGame(VENDOR_ID);
            when(gameRepositoryPort.findByVendorIdAndSlug(VENDOR_ID, "free-fire"))
                    .thenReturn(Optional.of(game));

            Game result = sut.getActiveBySlug(VENDOR_UUID, "free-fire");

            assertThat(result.getSlug()).isEqualTo("free-fire");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when game is inactive")
        void getActiveBySlug_shouldThrow_whenInactive() {
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            Game inactive = buildSavedGame(VENDOR_ID);
            inactive.setIsActive(false);
            when(gameRepositoryPort.findByVendorIdAndSlug(VENDOR_ID, "free-fire"))
                    .thenReturn(Optional.of(inactive));

            assertThatThrownBy(() -> sut.getActiveBySlug(VENDOR_UUID, "free-fire"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when slug does not exist")
        void getActiveBySlug_shouldThrow_whenNotFound() {
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            when(gameRepositoryPort.findByVendorIdAndSlug(VENDOR_ID, "unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.getActiveBySlug(VENDOR_UUID, "unknown"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Logical Delete")
    class DeleteTests {

        @Test
        @DisplayName("should perform logical delete by setting isActive to false")
        void delete_shouldSetIsActiveFalse() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Game existing = buildSavedGame(VENDOR_ID);
            assertThat(existing.getIsActive()).isTrue();

            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(existing));

            sut.delete(GAME_UUID, EXTERNAL_ID);

            assertThat(existing.getIsActive()).isFalse();
            verify(gameRepositoryPort).save(existing);
        }
    }
}
