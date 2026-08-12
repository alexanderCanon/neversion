package com.neversion.api.game.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.game.application.port.in.GameUseCase;
import com.neversion.api.game.domain.model.Game;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("GameController IT")
class GameControllerIT extends BaseWebIntegrationTest {


    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private static final UUID GAME_UUID = UUID.randomUUID();
    private static final UUID VENDOR_UUID = UUID.randomUUID();

    private String buildJwt(String role) throws Exception {
        byte[] secret = JWT_SECRET.getBytes();
        JWSSigner signer = new MACSigner(secret);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("supabase-user-" + UUID.randomUUID())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                .claim("app_metadata", Map.of("role", role))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    private Game mockGame() {
        return Game.builder()
                .id(1L)
                .uuid(GAME_UUID)
                .vendorId(10L)
                .name("Free Fire")
                .slug("free-fire")
                .imageUrl("https://example.com/image.png")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Map<String, Object> validRequestBody() {
        return Map.of(
                "name", "Free Fire",
                "slug", "free-fire",
                "imageUrl", "https://example.com/image.png"
        );
    }

    @Nested
    @DisplayName("Security & Access Control (RBAC)")
    class SecurityTests {

        @Test
        @DisplayName("POST /api/v1/games - should return 401 Unauthorized when no token is provided")
        void create_noToken_401() throws Exception {
            mockMvc.perform(post("/api/v1/games")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/games - should return 403 Forbidden with client role")
        void create_clientRole_403() throws Exception {
            String token = buildJwt("client");
            mockMvc.perform(post("/api/v1/games")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/v1/games - should return 201 Created with vendor role")
        void create_vendorRole_201() throws Exception {
            String token = buildJwt("vendor");
            when(gameUseCase.create(any(Game.class), anyString())).thenReturn(mockGame());

            mockMvc.perform(post("/api/v1/games")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(GAME_UUID.toString()))
                    .andExpect(jsonPath("$.slug").value("free-fire"))
                    .andExpect(jsonPath("$.name").value("Free Fire"));
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("POST /api/v1/games - should return 400 Bad Request if fields are invalid")
        void create_invalidFields_400() throws Exception {
            String token = buildJwt("vendor");
            Map<String, Object> invalidBody = Map.of(
                    "name", "",
                    "slug", "Invalid Slug With Spaces"
            );

            mockMvc.perform(post("/api/v1/games")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidBody)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Public Lookup")
    class PublicLookupTests {

        @Test
        @DisplayName("GET /api/v1/games/store/{vendorUuid} - should return 200 and allow public access")
        void storeCatalog_public_200() throws Exception {
            when(gameUseCase.listActive(any(UUID.class))).thenReturn(List.of(mockGame()));

            mockMvc.perform(get("/api/v1/games/store/" + VENDOR_UUID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(GAME_UUID.toString()));
        }

        @Test
        @DisplayName("GET /api/v1/games/store/{vendorUuid}/by-slug/{slug} - should return 200 and allow public access")
        void storeBySlug_public_200() throws Exception {
            when(gameUseCase.getActiveBySlug(eq(VENDOR_UUID), eq("free-fire")))
                    .thenReturn(mockGame());

            mockMvc.perform(get("/api/v1/games/store/" + VENDOR_UUID + "/by-slug/free-fire"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(GAME_UUID.toString()))
                    .andExpect(jsonPath("$.slug").value("free-fire"));
        }

        @Test
        @DisplayName("GET /api/v1/games/{id} - should return 200 and allow public access")
        void getById_public_200() throws Exception {
            when(gameUseCase.getById(any(UUID.class))).thenReturn(mockGame());

            mockMvc.perform(get("/api/v1/games/" + GAME_UUID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(GAME_UUID.toString()));
        }
    }
}
