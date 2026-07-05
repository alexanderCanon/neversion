package com.neversion.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OperationCustomizer customizeOperationId() {
        return (operation, handlerMethod) -> {
            String controller = handlerMethod.getBeanType().getSimpleName().replace("Controller", "");
            String method = handlerMethod.getMethod().getName();
            operation.setOperationId(method + controller);
            return operation;
        };
    }

    @Bean
    public OpenAPI panelOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Neversion Panel API")
                        .description(
                                "Admin panel API for service catalog, accounts, profiles, clients, reservations, orders, and subscriptions. "
                                        + "All mutating endpoints require a valid Supabase JWT with the 'admin' role.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Neversion")
                                .email("support@neversion.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Supabase JWT token. Obtain from Supabase Auth and paste here.")));
    }
}
