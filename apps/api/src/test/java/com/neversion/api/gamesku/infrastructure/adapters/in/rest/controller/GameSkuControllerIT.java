package com.neversion.api.gamesku.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.gamesku.application.port.in.GameSkuUseCase;
import com.neversion.api.gamesku.domain.model.GameSku;
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

import java.math.BigDecimal;
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

@DisplayName("GameSkuController IT")
class GameSkuControllerIT extends BaseWebIntegrationTest {


    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private static final UUID SKU_UUID = UUID.randomUUID();
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

    private GameSku mockGameSku() {
        return GameSku.builder()
                .id(1L)
                .uuid(SKU_UUID)
                .vendorId(10L)
                .gameId(5L)
                .code("ff-110")
                .name("Free Fire 110 Diamonds")
                .price(BigDecimal.valueOf(10.00))
                .imageUrl("https://example.com/image.png")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Map<String, Object> validRequestBody() {
        return Map.of(
                "code", "ff-110",
                "name", "Free Fire 110 Diamonds",
                "price", 10.00,
                "imageUrl", "https://example.com/image.png",
                "gameUuid", GAME_UUID.toString()
        );
    }

    @Nested
    @DisplayName("Security & Access Control (RBAC)")
    class SecurityTests {

        @Test
        @DisplayName("POST /api/v1/game-skus - should return 401 Unauthorized when no token is provided")
        void create_noToken_401() throws Exception {
            mockMvc.perform(post("/api/v1/game-skus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/game-skus - should return 403 Forbidden with client role")
        void create_clientRole_403() throws Exception {
            String token = buildJwt("client");
            mockMvc.perform(post("/api/v1/game-skus")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/v1/game-skus - should return 201 Created with vendor role")
        void create_vendorRole_201() throws Exception {
            String token = buildJwt("vendor");
            when(gameSkuUseCase.create(any(GameSku.class), anyString())).thenReturn(mockGameSku());

            mockMvc.perform(post("/api/v1/game-skus")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestBody())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(SKU_UUID.toString()))
                    .andExpect(jsonPath("$.code").value("ff-110"))
                    .andExpect(jsonPath("$.name").value("Free Fire 110 Diamonds"));
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("POST /api/v1/game-skus - should return 400 Bad Request if fields are invalid")
        void create_invalidFields_400() throws Exception {
            String token = buildJwt("vendor");
            Map<String, Object> invalidBody = Map.of(
                    "code", "",
                    "name", "",
                    "price", -5.00
            );

            mockMvc.perform(post("/api/v1/game-skus")
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
        @DisplayName("GET /api/v1/game-skus/store/{vendorUuid}?gameSlug=free-fire - should return 200 and allow public access")
        void storeBySlug_public_200() throws Exception {
            when(gameSkuUseCase.listActiveByGameSlug(eq(VENDOR_UUID), eq("free-fire")))
                    .thenReturn(List.of(mockGameSku()));

            mockMvc.perform(get("/api/v1/game-skus/store/" + VENDOR_UUID)
                            .param("gameSlug", "free-fire"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(SKU_UUID.toString()));
        }

        @Test
        @DisplayName("GET /api/v1/game-skus/{id} - should return 200 and allow public access")
        void getById_public_200() throws Exception {
            when(gameSkuUseCase.getById(any(UUID.class))).thenReturn(mockGameSku());

            mockMvc.perform(get("/api/v1/game-skus/" + SKU_UUID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(SKU_UUID.toString()));
        }
    }
}
