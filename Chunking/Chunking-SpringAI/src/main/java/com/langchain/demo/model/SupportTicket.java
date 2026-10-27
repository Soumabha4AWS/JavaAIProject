package com.langchain.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SupportTicket {
    @JsonProperty("ticket_id")
    private String ticketId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("category")
    private String category;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("description")
    private String description;

    @JsonProperty("resolution")
    private String resolution;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("resolved_date")
    private String resolvedDate;

    // Constructors
    public SupportTicket() {}

    public SupportTicket(String ticketId, String title, String category, String priority,
                        String description, String resolution, String createdDate, String resolvedDate) {
        this.ticketId = ticketId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.description = description;
        this.resolution = resolution;
        this.createdDate = createdDate;
        this.resolvedDate = resolvedDate;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "SupportTicket{" +
                "ticketId='" + ticketId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}
