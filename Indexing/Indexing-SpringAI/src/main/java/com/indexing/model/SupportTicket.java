package com.indexing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a support ticket from the JSON data.
 * Maps to synthetic_tickets.json structure.
 */
public class SupportTicket {
    @JsonProperty("ticket_id")
    private String ticketId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("resolution")
    private String resolution;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("created_date")
    private String createdDate;
    
    @JsonProperty("resolved_date")
    private String resolvedDate;

    public SupportTicket() {
    }

    public SupportTicket(String ticketId, String title, String description, 
                        String resolution, String category, String priority) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.resolution = resolution;
        this.category = category;
        this.priority = priority;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(String resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public String toRichContent() {
        return String.format(
            "Ticket ID: %s\n" +
            "Title: %s\n" +
            "Description: %s\n" +
            "Resolution: %s\n" +
            "Category: %s\n" +
            "Priority: %s",
            ticketId, title, description, resolution, category, priority
        );
    }

    @Override
    public String toString() {
        return "SupportTicket{" +
                "ticketId='" + ticketId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
