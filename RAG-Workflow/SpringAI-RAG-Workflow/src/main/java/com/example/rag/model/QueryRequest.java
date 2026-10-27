package com.example.rag.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request object containing a question for the RAG system")
public record QueryRequest(
        @NotBlank @Schema(description = "The question to ask the support ticket knowledge base", example = "How do I reset my password?")
        String question) {
}
