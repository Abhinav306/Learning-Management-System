package com.abhinav.lms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI-Powered LMS API")
                        .description("REST API for the AI-Powered Learning Management System")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Abhinav")
                                .email("abhinav@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server")
                ));
    }
}
