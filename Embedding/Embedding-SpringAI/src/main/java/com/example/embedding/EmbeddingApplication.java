package com.example.embedding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Embedding-SpringAI application.
 *
 * <p>On startup the application:
 * <ol>
 *   <li>Loads support tickets from {@code synthetic_tickets.json}</li>
 *   <li>Generates embeddings via the OpenAI API (using Spring AI)</li>
 *   <li>Computes cosine-similarity against a configurable query</li>
 *   <li>Prints the top-K most similar tickets</li>
 *   <li>Saves {@code similarity_matrix.csv} and {@code query_similarity.png}</li>
 * </ol>
 *
 * Configuration lives in {@code application.properties}.
 */
@SpringBootApplication
public class EmbeddingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddingApplication.class, args);
    }
}
