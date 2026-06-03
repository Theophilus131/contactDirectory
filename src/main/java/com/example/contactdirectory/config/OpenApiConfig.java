package com.example.contactdirectory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Customises the OpenAPI metadata visible in Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI contactDirectoryOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Contact Directory API")
                .description("""
                    A RESTful API for managing a personal contact directory.
                    
                    **Features**
                    - Full CRUD operations on contacts
                    - Case-insensitive search across firstName, lastName, and email
                    - Group-based filtering (FAMILY, FRIEND, WORK, OTHER)
                    - Combined search + group filtering
                    - Pagination and sorting support
                    - Email uniqueness enforcement
                    - ISO-8601 timestamps
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Developer")
                    .email("dev@example.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development server")
            ));
    }
}
