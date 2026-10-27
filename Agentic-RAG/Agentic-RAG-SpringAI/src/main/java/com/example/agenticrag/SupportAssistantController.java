package com.example.agenticrag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class SupportAssistantController {

    private final SupportAssistantService assistantService;

    public SupportAssistantController(SupportAssistantService assistantService) {
        this.assistantService = assistantService;
    }

        @Operation(summary = "Load support tickets",
            description = "Load synthetic_tickets.json from the application resources and rebuild the semantic index.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets imported",
                    content = @Content(schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "The bundled ticket file is invalid or empty")
    })
    @GetMapping(value = "/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> uploadTickets() {
        try {
            int count = assistantService.uploadTickets();
            return ResponseEntity.ok(new UploadResponse(count, "Tickets loaded and indexed successfully."));
        } catch (IllegalArgumentException | java.io.IOException exception) {
            return ResponseEntity.badRequest().body(new UploadResponse(0, exception.getMessage()));
        }
    }

    @Operation(summary = "Chat with the support assistant",
            description = "Maintains conversation history by conversationId and returns the rewritten search query used for the latest turn.")
        @PostMapping(value = "/back-and-forth", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SupportAssistantService.ChatResult chat(@RequestBody ChatRequest request) {
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? "default"
                : request.conversationId();
        return assistantService.ask(conversationId, request.message());
    }

    public record ChatRequest(String conversationId, String message) {
    }

    public record UploadResponse(int importedTickets, String message) {
    }
}
