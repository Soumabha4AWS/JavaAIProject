package com.example.agenticrag;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupportTicket(
        @JsonProperty("ticket_id") String ticketId,
        String title,
        String description,
        String resolution,
        String category,
        String priority,
        @JsonProperty("created_date") String createdDate,
        @JsonProperty("resolved_date") String resolvedDate) {

    public String asDocumentText() {
        return "Ticket ID: %s%nTitle: %s%nDescription: %s%nResolution: %s%nCategory: %s%nPriority: %s"
                .formatted(ticketId, title, description, resolution, category, priority);
    }
}
