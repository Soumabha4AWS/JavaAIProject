package com.example.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a support ticket loaded from {@code synthetic_tickets.json}.
 *
 * <p>Jackson deserialises each JSON object into this model.
 * Unknown fields are silently ignored so that the dataset can evolve without
 * breaking the application.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticket {

    @JsonProperty("ticket_id")
    public String ticket_id;

    @JsonProperty("title")
    public String title;

    @JsonProperty("description")
    public String description;

    @JsonProperty("resolution")
    public String resolution;

    @JsonProperty("category")
    public String category;

    @JsonProperty("priority")
    public String priority;

    @JsonProperty("created_date")
    public String created_date;

    @JsonProperty("resolved_date")
    public String resolved_date;

    /** Default constructor required by Jackson. */
    public Ticket() {}

    @Override
    public String toString() {
        return ticket_id + ": " + title;
    }
}
