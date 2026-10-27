package com.example.rag.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response from the RAG system containing the answer and source references")
public record RagResponse(
        @Schema(description = "The AI-generated answer to the user's question")
        String answer,
        @Schema(description = "List of source documents referenced to generate the answer")
        List<String> sources) {
}
