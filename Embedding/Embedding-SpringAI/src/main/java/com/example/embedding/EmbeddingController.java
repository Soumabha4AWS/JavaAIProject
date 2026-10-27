package com.example.embedding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

/**
 * REST controller that exposes the ticket-embedding similarity pipeline via HTTP.
 *
 * <p>All endpoints are documented via OpenAPI / Swagger-UI annotations.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Embedding Similarity", description = "Find semantically similar support tickets using OpenAI embeddings and cosine similarity")
public class EmbeddingController {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingController.class);

    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Value("${embedding.tickets-file:synthetic_tickets.json}")
    private String ticketsFile;

    @Value("${embedding.top-k:5}")
    private int defaultTopK;

    public EmbeddingController(EmbeddingService embeddingService, ObjectMapper objectMapper) {
        this.embeddingService = embeddingService;
        this.objectMapper = objectMapper;
    }

    // ── GET /api/tickets ──────────────────────────────────────────────────────

    @Operation(
        summary     = "List all tickets",
        description = "Returns the full list of support tickets loaded from the JSON dataset."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "Tickets retrieved successfully",
            content      = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Ticket.class))
        ),
        @ApiResponse(responseCode = "500", description = "Failed to load ticket dataset")
    })
    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> listTickets() {
        try {
            List<Ticket> tickets = loadTickets();
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Failed to load tickets", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── GET /api/tickets/{id} ─────────────────────────────────────────────────

    @Operation(
        summary     = "Get a ticket by ID",
        description = "Returns a single ticket matching the provided ticket ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket found"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/tickets/{id}")
    public ResponseEntity<Ticket> getTicket(
            @Parameter(description = "Ticket ID (e.g. TKT-001)", example = "TKT-001")
            @PathVariable String id) {
        try {
            return loadTickets().stream()
                    .filter(t -> id.equalsIgnoreCase(t.ticket_id))
                    .findFirst()
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Failed to find ticket {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── POST /api/embed/search ────────────────────────────────────────────────

    /**
     * Request body for the semantic search endpoint.
     */
    @Schema(description = "Semantic search request")
    public record SearchRequest(
            @Schema(description = "Free-text query to match against tickets",
                    example = "Users can't login after changing password",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String query,

            @Schema(description = "Maximum number of similar tickets to return (1–50)",
                    example = "5",
                    defaultValue = "5")
            Integer topK
    ) {}

    /**
     * Response item returned for each matched ticket.
     */
    @Schema(description = "A ticket with its cosine-similarity score")
    public record SimilarTicket(
            @Schema(description = "Rank position (1 = most similar)", example = "1")
            int rank,

            @Schema(description = "Cosine similarity score [0, 1]", example = "0.9123")
            double similarity,

            @Schema(description = "The matched ticket")
            Ticket ticket
    ) {}

    @Operation(
        summary     = "Find similar tickets",
        description = "Embeds the query text using OpenAI (`text-embedding-3-small`) then ranks " +
                      "all tickets by cosine similarity and returns the top-K results. " +
                      "Falls back to random embeddings when no API key is configured."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "Similar tickets ranked by cosine similarity",
            content      = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SimilarTicket.class))
        ),
        @ApiResponse(responseCode = "400", description = "Missing or empty query"),
        @ApiResponse(responseCode = "500", description = "Embedding or dataset error")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Search parameters",
        required    = true,
        content     = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples  = @ExampleObject(
                name  = "login-issue",
                value = """
                        {
                          "query": "Users can't login after changing password",
                          "topK": 5
                        }
                        """
            )
        )
    )
    @PostMapping("/embed/search")
    public ResponseEntity<List<SimilarTicket>> search(@RequestBody SearchRequest request) {

        if (request.query() == null || request.query().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        int k = (request.topK() != null && request.topK() > 0) ? request.topK() : defaultTopK;
        k = Math.min(k, 50);

        try {
            List<Ticket> tickets = loadTickets();

            // Build corpus texts
            List<String> texts = new ArrayList<>();
            for (Ticket t : tickets) texts.add(t.title + ". " + t.description);

            // Embed corpus & query
            double[][] corpusEmbeddings = embeddingService.embed(texts);
            double[]   queryEmbedding   = embeddingService.embed(List.of(request.query()))[0];

            // Cosine similarity for every ticket
            double[] scores = new double[tickets.size()];
            for (int i = 0; i < tickets.size(); i++) {
                scores[i] = embeddingService.cosineSimilarity(queryEmbedding, corpusEmbeddings[i]);
            }

            // Top-K indices sorted descending
            int finalK = Math.min(k, tickets.size());
            int[] indices = IntStream.range(0, scores.length)
                    .boxed()
                    .sorted((a, b) -> Double.compare(scores[b], scores[a]))
                    .mapToInt(Integer::intValue)
                    .limit(finalK)
                    .toArray();

            // Build response
            List<SimilarTicket> results = new ArrayList<>();
            for (int rank = 0; rank < indices.length; rank++) {
                int idx = indices[rank];
                results.add(new SimilarTicket(rank + 1, scores[idx], tickets.get(idx)));
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Search failed for query: {}", request.query(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── GET /api/embed/info ───────────────────────────────────────────────────

    @Operation(
        summary     = "Embedding model info",
        description = "Returns metadata about the configured embedding model and vector dimensionality."
    )
    @ApiResponse(responseCode = "200", description = "Model info retrieved")
    @GetMapping("/embed/info")
    public ResponseEntity<Map<String, Object>> embeddingInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("model",     "text-embedding-3-small");
        info.put("provider",  "OpenAI");
        info.put("dimension", embeddingService.getEmbeddingDim());
        info.put("similarity","cosine");
        return ResponseEntity.ok(info);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private List<Ticket> loadTickets() throws Exception {
        return objectMapper.readValue(
                new File(ticketsFile),
                new TypeReference<List<Ticket>>() {}
        );
    }
}
