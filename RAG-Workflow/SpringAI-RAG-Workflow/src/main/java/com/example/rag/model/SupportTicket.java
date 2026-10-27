package com.example.rag.model;

public record SupportTicket(
        String ticket_id,
        String title,
        String description,
        String resolution,
        String category,
        String priority,
        String created_date,
        String resolved_date) {

    public String asDocumentText() {
        return "Ticket ID: %s\nTitle: %s\nCategory: %s\nPriority: %s\nDate: %s to %s\n\nProblem Description:\n%s\n\nResolution:\n%s"
                .formatted(ticket_id, title, category, priority, created_date, resolved_date, description, resolution);
    }
}
