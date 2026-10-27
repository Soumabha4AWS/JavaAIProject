package com.interviewkickstart.rag;

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

    public String documentText() {
        return "Ticket ID: " + ticketId + "\n"
                + "Title: " + title + "\n"
                + "Category: " + category + "\n"
                + "Priority: " + priority + "\n"
                + "Date: " + createdDate + " to " + resolvedDate + "\n\n"
                + "Problem Description:\n" + description + "\n\n"
                + "Resolution:\n" + resolution;
    }
}