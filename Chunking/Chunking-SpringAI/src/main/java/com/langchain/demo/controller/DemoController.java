package com.langchain.demo.controller;

import com.langchain.demo.model.SupportTicket;
import com.langchain.demo.service.ChunkingService;
import com.langchain.demo.service.VectorStoreService;
import com.langchain.demo.util.DataLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Chunking & Vector Store API", description = "Endpoints for loading support tickets, comparing chunking strategies, and performing vector similarity search")
public class DemoController {

    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;

    @Value("${spring.ai.openai.api-key:dummy-key}")
    private String apiKey;

    public DemoController(ChunkingService chunkingService, VectorStoreService vectorStoreService) {
        this.chunkingService = chunkingService;
        this.vectorStoreService = vectorStoreService;
    }

    @Operation(
            summary = "Get all support tickets",
            description = "Loads and returns all synthetic support tickets from the local JSON file.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all support tickets returned successfully")
    })
    @GetMapping("/tickets")
    public List<SupportTicket> getTickets() throws IOException {
        return DataLoader.loadTickets("./synthetic_tickets.json");
    }

    @Operation(
            summary = "Compare chunking strategies",
            description = "Loads all tickets and applies three chunking strategies (fixed-size, recursive, whole-document) then returns chunk count and average length statistics for each strategy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chunking statistics returned successfully")
    })
    @GetMapping("/chunk")
    public Map<String, Object> getChunkStats() throws IOException {
        List<SupportTicket> tickets = DataLoader.loadTickets("./synthetic_tickets.json");
        List<Document> documents = tickets.stream()
                .map(ticket -> {
                    String content = DataLoader.formatTicketAsText(ticket);
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("ticket_id", ticket.getTicketId());
                    metadata.put("category", ticket.getCategory());
                    metadata.put("priority", ticket.getPriority());
                    metadata.put("title", ticket.getTitle());
                    return new Document(content, metadata);
                })
                .toList();

        List<Document> fixedChunks = chunkingService.fixedSizeChunking(documents, 200, 20);
        List<Document> recursiveChunks = chunkingService.recursiveChunking(documents, 300, 50);
        List<Document> wholeDocChunks = chunkingService.wholeDocumentChunking(documents);

        Map<String, Object> response = new HashMap<>();
        response.put("originalCount", documents.size());
        
        response.put("fixedSize", Map.of(
            "chunkCount", fixedChunks.size(),
            "averageLength", fixedChunks.stream().mapToInt(c -> c.getContent().length()).average().orElse(0.0)
        ));
        
        response.put("recursiveSplitting", Map.of(
            "chunkCount", recursiveChunks.size(),
            "averageLength", recursiveChunks.stream().mapToInt(c -> c.getContent().length()).average().orElse(0.0)
        ));
        
        response.put("wholeDocuments", Map.of(
            "chunkCount", wholeDocChunks.size(),
            "averageLength", wholeDocChunks.stream().mapToInt(c -> c.getContent().length()).average().orElse(0.0)
        ));

        return response;
    }

    @Operation(
            summary = "Semantic similarity search",
            description = "Performs a vector similarity search over stored document embeddings. Requires a valid OpenAI API key to be configured.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Similar documents returned successfully"),
            @ApiResponse(responseCode = "503", description = "OpenAI API key not configured")
    })
    @GetMapping("/search")
    public List<VectorStoreService.StoredDocument> search(
            @Parameter(description = "Natural language query to search for", required = true) @RequestParam String query,
            @Parameter(description = "Maximum number of results to return", example = "3") @RequestParam(defaultValue = "3") int limit) throws IOException {
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("dummy-key")) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OPENAI_API_KEY environment variable is not set. Please set it to enable vector search.");
        }

        // Populate the store if empty
        if (vectorStoreService.getDocumentCount() == 0) {
            List<SupportTicket> tickets = DataLoader.loadTickets("./synthetic_tickets.json");
            List<Document> documents = tickets.stream()
                    .map(ticket -> {
                        String content = DataLoader.formatTicketAsText(ticket);
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("ticket_id", ticket.getTicketId());
                        metadata.put("category", ticket.getCategory());
                        metadata.put("priority", ticket.getPriority());
                        metadata.put("title", ticket.getTitle());
                        return new Document(content, metadata);
                    })
                    .toList();
            vectorStoreService.addDocuments(documents);
        }

        return vectorStoreService.similaritySearch(query, limit);
    }

    @Operation(
            summary = "Filtered semantic similarity search",
            description = "Performs a vector similarity search filtered by a specific metadata key-value pair (e.g. filterKey=category, filterValue=Billing). Requires a valid OpenAI API key.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered similar documents returned successfully"),
            @ApiResponse(responseCode = "503", description = "OpenAI API key not configured")
    })
    @GetMapping("/search-filtered")
    public List<VectorStoreService.StoredDocument> searchFiltered(
            @Parameter(description = "Natural language query to search for", required = true) @RequestParam String query,
            @Parameter(description = "Metadata field name to filter by (e.g. category, priority)", required = true) @RequestParam String filterKey,
            @Parameter(description = "Metadata field value to filter by (e.g. Billing, High)", required = true) @RequestParam String filterValue,
            @Parameter(description = "Maximum number of results to return", example = "3") @RequestParam(defaultValue = "3") int limit) throws IOException {
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("dummy-key")) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OPENAI_API_KEY environment variable is not set. Please set it to enable vector search.");
        }

        // Populate the store if empty
        if (vectorStoreService.getDocumentCount() == 0) {
            List<SupportTicket> tickets = DataLoader.loadTickets("./synthetic_tickets.json");
            List<Document> documents = tickets.stream()
                    .map(ticket -> {
                        String content = DataLoader.formatTicketAsText(ticket);
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("ticket_id", ticket.getTicketId());
                        metadata.put("category", ticket.getCategory());
                        metadata.put("priority", ticket.getPriority());
                        metadata.put("title", ticket.getTitle());
                        return new Document(content, metadata);
                    })
                    .toList();
            vectorStoreService.addDocuments(documents);
        }

        return vectorStoreService.similaritySearchWithFilter(query, limit, filterKey, filterValue);
    }
}
