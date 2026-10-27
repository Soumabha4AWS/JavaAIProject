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
        return "Ticket ID: " + ticketId + "\n"
                + "Title: " + title + "\n"
                + "Description: " + description + "\n"
                + "Resolution: " + resolution + "\n"
                + "Category: " + category + "\n"
                + "Priority: " + priority;
    }

    public String asFullText() {
        return asDocumentText() + "\n"
                + "Created: " + createdDate + "\n"
                + "Resolved: " + resolvedDate;
    }
}
