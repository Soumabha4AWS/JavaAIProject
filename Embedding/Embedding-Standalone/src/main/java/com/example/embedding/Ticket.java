package com.example.embedding;

public class Ticket {
    public String ticket_id;
    public String title;
    public String description;
    public String resolution;
    public String category;
    public String priority;
    public String created_date;
    public String resolved_date;

    // Default constructor for Jackson
    public Ticket() {}

    @Override
    public String toString() {
        return ticket_id + ": " + title;
    }
}
