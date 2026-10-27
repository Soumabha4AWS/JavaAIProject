package com.langchain.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Chunking & Vector Store API")
                        .description("REST API for demonstrating text chunking strategies and vector store similarity search using Spring AI.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Spring AI Demo")
                                .email("demo@langchain.com")));
    }
}
