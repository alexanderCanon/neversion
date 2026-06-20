package com.neversion.api.user.infrastructure.adapters.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neversion.api.user.application.port.out.AuthServicePort;
import com.neversion.api.user.domain.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST adapter for AuthServicePort calling the external auth service via HTTP.
 */
@Component
public class RestAuthAdapter implements AuthServicePort {

    private final RestTemplate restTemplate;
    private final String authApiUrl;
    private final String adminKey;

    public RestAuthAdapter(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${auth.api-url}") String authApiUrl,
            @Value("${auth.admin-key}") String adminKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.authApiUrl = authApiUrl;
        this.adminKey = adminKey;
    }

    @Override
    public String createUser(String email, String password, UserRole role) {
        String url = authApiUrl + "/auth/v1/admin/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", adminKey);
        headers.setBearerAuth(adminKey);

        CreateUserRequest requestBody = new CreateUserRequest(
                email,
                password,
                true, // email_confirm
                Map.of(
                        "provider", "email",
                        "providers", List.of("email"),
                        "role", role.name().toLowerCase()
                ) // app_metadata
        );

        HttpEntity<CreateUserRequest> request = new HttpEntity<>(requestBody, headers);

        try {
            CreateUserResponse response = restTemplate.postForObject(
                    url, request, CreateUserResponse.class);

            if (response == null || response.id() == null) {
                throw new IllegalStateException("Failed to create user in auth service: no ID returned");
            }

            return response.id();
        } catch (Exception e) {
            throw new IllegalStateException("Error calling auth service Admin API: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<String> findEmailByExternalId(String externalId) {
        String url = authApiUrl + "/auth/v1/admin/users/" + externalId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", adminKey);
        headers.setBearerAuth(adminKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    UserResponse.class);

            if (response.getBody() == null || response.getBody().email() == null
                    || response.getBody().email().isBlank()) {
                return Optional.empty();
            }

            return Optional.of(response.getBody().email());
        } catch (Exception e) {
            throw new IllegalStateException("Error resolving user email from auth service: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateAppMetadata(String externalId, UserRole role) {
        String url = authApiUrl + "/auth/v1/admin/users/" + externalId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", adminKey);
        headers.setBearerAuth(adminKey);

        UpdateAppMetadataRequest requestBody = new UpdateAppMetadataRequest(
                Map.of("role", role.name().toLowerCase())
        );

        HttpEntity<UpdateAppMetadataRequest> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error updating app_metadata for user " + externalId + ": " + e.getMessage(), e);
        }
    }

    // Records for JSON mapping
    private record CreateUserRequest(
            String email,
            String password,
            @JsonProperty("email_confirm") boolean emailConfirm,
            @JsonProperty("app_metadata") Map<String, Object> appMetadata
    ) {}

    private record UpdateAppMetadataRequest(
            @JsonProperty("app_metadata") Map<String, Object> appMetadata
    ) {}

    private record CreateUserResponse(
            String id
    ) {}

    private record UserResponse(
            String id,
            String email
    ) {}
}
