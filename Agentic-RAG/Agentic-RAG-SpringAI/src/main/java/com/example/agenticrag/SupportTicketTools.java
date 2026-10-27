package com.example.agenticrag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SupportTicketTools {

    private final ObjectMapper objectMapper;
    private final EmbeddingModel embeddingModel;
    private List<SupportTicket> tickets;
    private VectorStore vectorStore;

    public SupportTicketTools(ObjectMapper objectMapper, EmbeddingModel embeddingModel) {
        this.objectMapper = objectMapper;
        this.embeddingModel = embeddingModel;
    }

    @PostConstruct
    void initialize() throws IOException {
                loadTicketsFromResource();
        }

        public int loadTicketsFromResource() throws IOException {
                try (InputStream input = new ClassPathResource("synthetic_tickets.json").getInputStream()) {
                        return replaceTickets(input);
                }
        }

        public synchronized int replaceTickets(InputStream input) throws IOException {
                List<SupportTicket> importedTickets = objectMapper.readValue(input, new TypeReference<>() {
                });
                if (importedTickets == null || importedTickets.isEmpty()) {
                        throw new IllegalArgumentException("The ticket file must contain at least one ticket.");
                }

                VectorStore importedVectorStore = SimpleVectorStore.builder(embeddingModel).build();
                importedVectorStore.add(importedTickets.stream()
                .map(ticket -> new Document(ticket.asDocumentText(), Map.of(
                        "ticket_id", ticket.ticketId(),
                        "category", ticket.category(),
                        "priority", ticket.priority(),
                        "title", ticket.title())))
                .toList());
        tickets = importedTickets;
        vectorStore = importedVectorStore;
        return tickets.size();
    }

    @Tool(description = "Search similar support tickets for a troubleshooting question or problem description.")
    public String searchSimilarTickets(String query) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(3).build());
        if (results.isEmpty()) {
            return "No similar tickets found.";
        }
        StringBuilder output = new StringBuilder("Found similar tickets:\n\n");
        for (int index = 0; index < results.size(); index++) {
            output.append("--- Ticket ").append(index + 1).append(" ---\n")
                    .append(results.get(index).getText()).append("\n\n");
        }
        return output.toString();
    }

    @Tool(description = "Retrieve complete details for a specific ticket ID such as TICK-001.")
    public String getTicketById(String ticketId) {
        String normalizedId = ticketId.toUpperCase().trim();
        return tickets.stream()
                .filter(ticket -> ticket.ticketId().equals(normalizedId))
                .findFirst()
                .map(this::formatTicket)
                .orElse("Ticket %s not found in the database.".formatted(normalizedId));
    }

    @Tool(description = "Find all support tickets in an exact category such as Authentication, Database, or Payment.")
    public String searchByCategory(String category) {
        String normalizedCategory = category.trim();
        List<SupportTicket> matching = tickets.stream()
                .filter(ticket -> ticket.category().equalsIgnoreCase(normalizedCategory))
                .toList();
        if (matching.isEmpty()) {
            String available = tickets.stream().map(SupportTicket::category).distinct().sorted()
                    .collect(Collectors.joining(", "));
            return "No tickets found in category '%s'. Available categories: %s"
                    .formatted(normalizedCategory, available);
        }
        String entries = matching.stream()
                .map(ticket -> "  - [%s] %s (Priority: %s)".formatted(
                        ticket.ticketId(), ticket.title(), ticket.priority()))
                .collect(Collectors.joining("\n"));
        return "Found %d tickets in category '%s':\n\n%s"
                .formatted(matching.size(), normalizedCategory, entries);
    }

    @Tool(description = "Get total ticket count, category distribution, and priority breakdown.")
    public String getTicketStatistics() {
        Map<String, Long> categories = countBy(SupportTicket::category);
        Map<String, Long> priorities = countBy(SupportTicket::priority);
        return "Ticket Database Statistics:\nTotal Tickets: %d\n\nBy Category:\n%s\n\nBy Priority:\n%s"
                .formatted(tickets.size(), formatCounts(categories), formatCounts(priorities));
    }

    private String formatTicket(SupportTicket ticket) {
        return "Ticket ID: %s%nTitle: %s%nDescription: %s%nResolution: %s%nCategory: %s%nPriority: %s%nCreated: %s%nResolved: %s"
                .formatted(ticket.ticketId(), ticket.title(), ticket.description(), ticket.resolution(),
                        ticket.category(), ticket.priority(), ticket.createdDate(), ticket.resolvedDate());
    }

    private Map<String, Long> countBy(java.util.function.Function<SupportTicket, String> key) {
        return tickets.stream().collect(Collectors.groupingBy(key, Collectors.counting()));
    }

    private String formatCounts(Map<String, Long> counts) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> "  - %s: %d".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
    }
}
