package com.neversion.api.gamesku.application.service;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.domain.port.out.GameRepositoryPort;
import com.neversion.api.gamesku.domain.model.GameSku;
import com.neversion.api.gamesku.domain.port.out.GameSkuRepositoryPort;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameSkuService unit tests")
class GameSkuServiceUT {

    @Mock
    private GameSkuRepositoryPort gameSkuRepositoryPort;
    @Mock
    private GameRepositoryPort gameRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private VendorRepositoryPort vendorRepositoryPort;

    private GameSkuService sut;

    private static final String EXTERNAL_ID = "supabase-user-uuid-123";
    private static final Long USER_ID = 10L;
    private static final Long VENDOR_ID = 20L;
    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final UUID GAME_UUID = UUID.randomUUID();
    private static final UUID SKU_UUID = UUID.randomUUID();
    private static final Long GAME_ID = 5L;

    @BeforeEach
    void setUp() {
        sut = new GameSkuService(gameSkuRepositoryPort, gameRepositoryPort,
                userRepositoryPort, vendorRepositoryPort);
    }

    private void stubCallerChain(String extId, Long userId, Long vendorId) {
        User user = User.builder().id(userId).externalId(extId).build();

        Vendor vendor = Vendor.builder().id(vendorId).uuid(VENDOR_UUID).userId(userId).build();

        when(userRepositoryPort.findByExternalId(extId)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(vendor));
    }

    private Game buildParentGame() {
        return Game.builder()
                .id(GAME_ID)
                .uuid(GAME_UUID)
                .vendorId(VENDOR_ID)
                .name("Free Fire")
                .slug("free-fire")
                .isActive(true)
                .build();
    }

    private GameSku buildSkuInput() {
        return GameSku.builder()
                .code("ff-110")
                .name("Free Fire 110 Diamonds")
                .price(new BigDecimal("10.00"))
                .imageUrl("https://image.url/ff110.png")
                .gameUuid(GAME_UUID)
                .build();
    }

    private GameSku buildSavedSku(Long vendorId) {
        return GameSku.builder()
                .id(1L)
                .uuid(SKU_UUID)
                .vendorId(vendorId)
                .gameId(GAME_ID)
                .code("ff-110")
                .name("Free Fire 110 Diamonds")
                .price(new BigDecimal("10.00"))
                .imageUrl("https://image.url/ff110.png")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Create")
    class CreateTests {

        @Test
        @DisplayName("should create SKU with gameId resolved from gameUuid")
        void create_shouldResolveGameUuidAndSave() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku input = buildSkuInput();
            GameSku saved = buildSavedSku(VENDOR_ID);

            when(gameSkuRepositoryPort.existsByVendorIdAndCode(VENDOR_ID, "ff-110")).thenReturn(false);
            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(buildParentGame()));
            when(gameSkuRepositoryPort.save(any())).thenReturn(saved);

            GameSku result = sut.create(input, EXTERNAL_ID);

            assertThat(result.getVendorId()).isEqualTo(VENDOR_ID);
            assertThat(result.getGameId()).isEqualTo(GAME_ID);
            assertThat(result.getIsActive()).isTrue();
            verify(gameSkuRepositoryPort).save(any());
        }

        @Test
        @DisplayName("should throw BusinessRuleException when code already exists for vendor")
        void create_shouldThrow_whenDuplicateCode() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku input = buildSkuInput();

            when(gameSkuRepositoryPort.existsByVendorIdAndCode(VENDOR_ID, "ff-110")).thenReturn(true);

            assertThatThrownBy(() -> sut.create(input, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("ff-110");

            verify(gameSkuRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when gameUuid does not exist")
        void create_shouldThrow_whenGameNotFound() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku input = buildSkuInput();

            when(gameSkuRepositoryPort.existsByVendorIdAndCode(VENDOR_ID, "ff-110")).thenReturn(false);
            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.create(input, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(gameSkuRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when game belongs to another vendor")
        void create_shouldThrow_whenGameOwnedByOtherVendor() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku input = buildSkuInput();
            Game otherVendorGame = Game.builder()
                    .id(GAME_ID)
                    .uuid(GAME_UUID)
                    .vendorId(999L)
                    .name("Free Fire")
                    .slug("free-fire")
                    .isActive(true)
                    .build();

            when(gameSkuRepositoryPort.existsByVendorIdAndCode(VENDOR_ID, "ff-110")).thenReturn(false);
            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(otherVendorGame));

            assertThatThrownBy(() -> sut.create(input, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(gameSkuRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update")
    class UpdateTests {

        @Test
        @DisplayName("should update SKU when caller owns it")
        void update_shouldSave_whenCallerIsOwner() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku existing = buildSavedSku(VENDOR_ID);
            GameSku updatedInput = GameSku.builder()
                    .code("ff-110-updated")
                    .name("Updated Diamonds")
                    .price(new BigDecimal("15.00"))
                    .imageUrl("https://new.url")
                    .gameUuid(GAME_UUID)
                    .build();

            when(gameSkuRepositoryPort.findById(SKU_UUID)).thenReturn(Optional.of(existing));
            when(gameSkuRepositoryPort.existsByVendorIdAndCode(VENDOR_ID, "ff-110-updated")).thenReturn(false);
            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(buildParentGame()));
            when(gameSkuRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            GameSku result = sut.update(SKU_UUID, updatedInput, EXTERNAL_ID);

            assertThat(result.getCode()).isEqualTo("ff-110-updated");
            assertThat(result.getName()).isEqualTo("Updated Diamonds");
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("15.00"));
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own the SKU")
        void update_shouldThrowAccessDenied_whenNotOwner() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku existing = buildSavedSku(999L);
            GameSku updatedInput = buildSkuInput();

            when(gameSkuRepositoryPort.findById(SKU_UUID)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> sut.update(SKU_UUID, updatedInput, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(gameSkuRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("List By Vendor")
    class ListByVendorTests {

        @Test
        @DisplayName("should filter SKUs by gameUuid when provided")
        void listByVendor_shouldFilterByGame_whenGameUuidProvided() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            when(gameRepositoryPort.findById(GAME_UUID)).thenReturn(Optional.of(buildParentGame()));
            when(gameSkuRepositoryPort.findByVendorIdAndGameId(VENDOR_ID, GAME_ID))
                    .thenReturn(List.of(buildSavedSku(VENDOR_ID)));

            var result = sut.listByVendor(VENDOR_UUID, GAME_UUID, null, EXTERNAL_ID);

            assertThat(result).hasSize(1);
            verify(gameSkuRepositoryPort).findByVendorIdAndGameId(VENDOR_ID, GAME_ID);
            verify(gameSkuRepositoryPort, never()).findAllByVendorId(any());
        }

        @Test
        @DisplayName("should return all vendor SKUs when gameUuid is not provided")
        void listByVendor_shouldReturnAll_whenNoGameFilter() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            when(gameSkuRepositoryPort.findAllByVendorId(VENDOR_ID))
                    .thenReturn(List.of(buildSavedSku(VENDOR_ID)));

            var result = sut.listByVendor(VENDOR_UUID, null, null, EXTERNAL_ID);

            assertThat(result).hasSize(1);
            verify(gameSkuRepositoryPort).findAllByVendorId(VENDOR_ID);
        }
    }

    @Nested
    @DisplayName("List Active By Game Slug")
    class ListActiveByGameSlugTests {

        @Test
        @DisplayName("should return active SKUs for a game identified by slug")
        void listActiveByGameSlug_shouldReturnSkus_whenGameActive() {
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            when(gameRepositoryPort.findByVendorIdAndSlug(VENDOR_ID, "free-fire"))
                    .thenReturn(Optional.of(buildParentGame()));
            when(gameSkuRepositoryPort.findActiveByVendorIdAndGameId(VENDOR_ID, GAME_ID))
                    .thenReturn(List.of(buildSavedSku(VENDOR_ID)));

            var result = sut.listActiveByGameSlug(VENDOR_UUID, "free-fire");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when game slug does not exist")
        void listActiveByGameSlug_shouldThrow_whenGameNotFound() {
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            when(gameRepositoryPort.findByVendorIdAndSlug(VENDOR_ID, "unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.listActiveByGameSlug(VENDOR_UUID, "unknown"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when game is inactive")
        void listActiveByGameSlug_shouldThrow_whenGameInactive() {
            Vendor vendor = Vendor.builder().id(VENDOR_ID).uuid(VENDOR_UUID).userId(USER_ID).build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            Game inactive = buildParentGame();
            inactive.setIsActive(false);
            when(gameRepositoryPort.findByVendorIdAndSlug(VENDOR_ID, "free-fire"))
                    .thenReturn(Optional.of(inactive));

            assertThatThrownBy(() -> sut.listActiveByGameSlug(VENDOR_UUID, "free-fire"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Toggle Status")
    class ToggleStatusTests {

        @Test
        @DisplayName("should toggle active status")
        void toggle_shouldInvertIsActive() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku existing = buildSavedSku(VENDOR_ID);
            assertThat(existing.getIsActive()).isTrue();

            when(gameSkuRepositoryPort.findById(SKU_UUID)).thenReturn(Optional.of(existing));
            when(gameSkuRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            GameSku result = sut.toggleStatus(SKU_UUID, EXTERNAL_ID);

            assertThat(result.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Logical Delete")
    class DeleteTests {

        @Test
        @DisplayName("should perform logical delete by setting isActive to false")
        void delete_shouldSetIsActiveFalse() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            GameSku existing = buildSavedSku(VENDOR_ID);
            assertThat(existing.getIsActive()).isTrue();

            when(gameSkuRepositoryPort.findById(SKU_UUID)).thenReturn(Optional.of(existing));

            sut.delete(SKU_UUID, EXTERNAL_ID);

            assertThat(existing.getIsActive()).isFalse();
            verify(gameSkuRepositoryPort).save(existing);
        }
    }
}
