package com.example.rag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI RAG Workflow API")
                        .description("Retrieval-Augmented Generation API for support ticket Q&A with conversation support")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("RAG Workflow Team")
                                .url("https://github.com")));
    }
}
