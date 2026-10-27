package com.example.embedding;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI / Swagger-UI metadata for the Embedding-SpringAI application.
 *
 * <p>Swagger UI is available at: {@code http://localhost:{server.port}/swagger-ui.html}
 * <br>OpenAPI JSON spec  is at: {@code http://localhost:{server.port}/v3/api-docs}
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI embeddingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Embedding Similarity API")
                        .description(
                                "REST API for the **Ticket Embedding Similarity** demo built with " +
                                "Spring Boot 3 and Spring AI.\n\n" +
                                "Use the `/api/tickets` endpoint to browse loaded tickets, " +
                                "and `/api/embed/search` to find the most similar tickets " +
                                "to any free-text query using OpenAI embeddings + cosine similarity."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Embedding SpringAI Demo")
                                .email("demo@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }
}
