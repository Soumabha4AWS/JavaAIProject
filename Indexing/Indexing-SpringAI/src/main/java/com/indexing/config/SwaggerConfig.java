package com.indexing.config;

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
 * Swagger / OpenAPI 3 configuration.
 *
 * UI  : http://localhost:{port}/swagger-ui.html
 * JSON: http://localhost:{port}/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI llamaIndexOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LlamaIndex Java – Indexing Strategies API")
                        .description("""
                                REST API for querying documents using four indexing strategies:
                                **Vector**, **Summary**, **Keyword**, and **Hybrid**.
                                
                                Each query endpoint accepts a natural-language question and returns
                                a synthesised answer together with the source documents that were
                                retrieved from the underlying index.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Indexing Demo")
                                .email("demo@indexing.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }
}
