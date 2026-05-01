package com.neversion.api.user.infrastructure.adapters.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neversion.api.user.application.port.out.SupabaseAuthPort;
import com.neversion.api.user.domain.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SupabaseAuthAdapter implements SupabaseAuthPort {

    private final RestTemplate restTemplate;
    private final String supabaseUrl;
    private final String serviceRoleKey;

    public SupabaseAuthAdapter(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
    }

    @Override
    public String createUser(String email, String password, UserRole role) {
        String url = supabaseUrl + "/auth/v1/admin/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", serviceRoleKey);
        headers.setBearerAuth(serviceRoleKey);

        SupabaseCreateUserRequest requestBody = new SupabaseCreateUserRequest(
                email,
                password,
                true, // email_confirm
                Map.of("role", role.name().toLowerCase()) // app_metadata
        );

        HttpEntity<SupabaseCreateUserRequest> request = new HttpEntity<>(requestBody, headers);

        try {
            SupabaseCreateUserResponse response = restTemplate.postForObject(
                    url, request, SupabaseCreateUserResponse.class);

            if (response == null || response.id() == null) {
                throw new IllegalStateException("Failed to create user in Supabase: no ID returned");
            }

            return response.id();
        } catch (Exception e) {
            throw new IllegalStateException("Error calling Supabase Admin API: " + e.getMessage(), e);
        }
    }

    // Records for JSON mapping
    private record SupabaseCreateUserRequest(
            String email,
            String password,
            @JsonProperty("email_confirm") boolean emailConfirm,
            @JsonProperty("app_metadata") Map<String, Object> appMetadata
    ) {}

    private record SupabaseCreateUserResponse(
            String id
    ) {}
}
