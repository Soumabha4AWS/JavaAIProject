package com.indexing.controller;

import com.indexing.index.HybridIndex;
import com.indexing.index.KeywordIndex;
import com.indexing.index.SummaryIndex;
import com.indexing.index.VectorIndex;
import com.indexing.query.HybridQueryEngine;
import com.indexing.query.KeywordQueryEngine;
import com.indexing.query.QueryResponse;
import com.indexing.query.SummaryQueryEngine;
import com.indexing.query.VectorQueryEngine;
import com.indexing.service.IndexInitializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
@Tag(name = "Query API", description = "Query documents using Vector, Summary, Keyword, and Hybrid indexing strategies")
public class QueryController {

    private final VectorQueryEngine vectorQueryEngine;
    private final SummaryQueryEngine summaryQueryEngine;
    private final KeywordQueryEngine keywordQueryEngine;
    private final HybridQueryEngine hybridQueryEngine;
    private final IndexInitializationService initService;

    public QueryController(VectorIndex vectorIndex,
                           SummaryIndex summaryIndex,
                           KeywordIndex keywordIndex,
                           HybridIndex hybridIndex,
                           IndexInitializationService initService) {
        this.vectorQueryEngine  = new VectorQueryEngine(vectorIndex);
        this.summaryQueryEngine = new SummaryQueryEngine(summaryIndex);
        this.keywordQueryEngine = new KeywordQueryEngine(keywordIndex);
        this.hybridQueryEngine  = new HybridQueryEngine(hybridIndex);
        this.initService        = initService;
    }

    /** GET /api/query/vector?q=... */
    @Operation(
        summary = "Query via Vector Index",
        description = "Uses semantic similarity (cosine distance over OpenAI embeddings) to retrieve the top-K most relevant document chunks and synthesise an answer."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Query succeeded",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QueryResponse.class))),
        @ApiResponse(responseCode = "503", description = "Vector index is still warming up", content = @Content)
    })
    @GetMapping("/vector")
    public ResponseEntity<?> queryVector(
            @Parameter(description = "Natural-language question to ask the index", required = true, example = "What is the refund policy?")
            @RequestParam String q) throws IOException {
        if (!initService.isVectorReady()) {
            return warming("vector");
        }
        return ResponseEntity.ok(vectorQueryEngine.query(q));
    }

    /** GET /api/query/summary?q=... */
    @Operation(
        summary = "Query via Summary Index",
        description = "Uses LLM-generated per-document summaries to find relevant documents, then synthesises a final answer from those summaries."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Query succeeded",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QueryResponse.class))),
        @ApiResponse(responseCode = "503", description = "Summary index is still warming up", content = @Content)
    })
    @GetMapping("/summary")
    public ResponseEntity<?> querySummary(
            @Parameter(description = "Natural-language question to ask the index", required = true, example = "Summarise open bugs filed last week")
            @RequestParam String q) throws IOException {
        if (!initService.isSummaryReady()) {
            return warming("summary");
        }
        return ResponseEntity.ok(summaryQueryEngine.query(q));
    }

    /** GET /api/query/keyword?q=... */
    @Operation(
        summary = "Query via Keyword Index",
        description = "Uses BM25-style keyword matching to retrieve documents that contain the most query terms, then synthesises an answer."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Query succeeded",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QueryResponse.class))),
        @ApiResponse(responseCode = "503", description = "Keyword index is still warming up", content = @Content)
    })
    @GetMapping("/keyword")
    public ResponseEntity<?> queryKeyword(
            @Parameter(description = "Natural-language question to ask the index", required = true, example = "login authentication error")
            @RequestParam String q) throws IOException {
        if (!initService.isKeywordReady()) {
            return warming("keyword");
        }
        return ResponseEntity.ok(keywordQueryEngine.query(q));
    }

    /** GET /api/query/hybrid?q=... */
    @Operation(
        summary = "Query via Hybrid Index",
        description = "Combines Vector and Keyword retrieval with reciprocal rank fusion to get the best of both strategies."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Query succeeded",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QueryResponse.class))),
        @ApiResponse(responseCode = "503", description = "Hybrid index is still warming up", content = @Content)
    })
    @GetMapping("/hybrid")
    public ResponseEntity<?> queryHybrid(
            @Parameter(description = "Natural-language question to ask the index", required = true, example = "How do I reset my password?")
            @RequestParam String q) throws IOException {
        if (!initService.isHybridReady()) {
            return warming("hybrid");
        }
        return ResponseEntity.ok(hybridQueryEngine.query(q));
    }

    /** GET /api/query/status — check which indexes are ready */
    @Operation(
        summary = "Index readiness status",
        description = "Returns a map indicating whether each index (vector, summary, keyword, hybrid) has finished building and is ready to accept queries."
    )
    @ApiResponse(responseCode = "200", description = "Status map returned successfully")
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
            "vector",  initService.isVectorReady(),
            "summary", initService.isSummaryReady(),
            "keyword", initService.isKeywordReady(),
            "hybrid",  initService.isHybridReady()
        );
    }

    private ResponseEntity<Map<String, String>> warming(String index) {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status",  "warming_up",
                "message", "The '" + index + "' index is still being built. Please try again in a moment.",
                "hint",    "Check /api/query/status for readiness of all indexes."
            ));
    }
}
