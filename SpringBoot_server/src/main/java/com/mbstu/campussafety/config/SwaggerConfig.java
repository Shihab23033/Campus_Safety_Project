package com.mbstu.campussafety.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Campus Safety API")
                .version("1.0.0")
                .description("Backend API for Campus Safety Emergency Response System")
                .contact(new Contact()
                    .name("Campus Safety Team")
                    .email("support@campussafety.com")
                    .url("https://campussafety.com")
                )
            )
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")
                )
            )
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication")
            );
    }
}
