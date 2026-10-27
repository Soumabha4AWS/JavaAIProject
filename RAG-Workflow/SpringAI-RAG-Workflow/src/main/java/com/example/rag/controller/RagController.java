package com.example.rag.controller;

import com.example.rag.model.QueryRequest;
import com.example.rag.model.RagResponse;
import com.example.rag.service.ConversationService;
import com.example.rag.service.RagService;
import com.example.rag.service.TicketIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/rag", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "RAG Operations", description = "APIs for Retrieval-Augmented Generation support ticket queries")
public class RagController {

    private final RagService ragService;
    private final ConversationService conversationService;
    private final TicketIngestionService ticketIngestionService;

    public RagController(RagService ragService, ConversationService conversationService,
                         TicketIngestionService ticketIngestionService) {
        this.ragService = ragService;
        this.conversationService = conversationService;
        this.ticketIngestionService = ticketIngestionService;
    }

    @PostMapping(path = "/tickets/load")
    @Operation(summary = "Load support tickets", description = "Load support tickets from the configured source into the RAG vector store")
    public Map<String, Integer> loadTickets() {
        return Map.of("ticketCount", ticketIngestionService.loadTickets());
    }

    @PostMapping(path = "/query", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Query support tickets", description = "Submit a question to get relevant answers from support ticket database using RAG")
    public RagResponse query(@Valid @RequestBody QueryRequest request) {
        return ragService.answer(request.question());
    }

    @PostMapping(path = "/conversations/{conversationId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Send message in conversation", description = "Send a message in a specific conversation thread to maintain context across multiple queries")
    public RagResponse message(@PathVariable String conversationId, @Valid @RequestBody QueryRequest request) {
        return conversationService.answer(conversationId, request.question());
    }
}
