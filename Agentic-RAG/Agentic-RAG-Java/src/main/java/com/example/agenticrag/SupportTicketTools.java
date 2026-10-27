package com.example.agenticrag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SupportTicketTools {
    private final List<SupportTicket> tickets;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<Document> embeddingStore;

    public SupportTicketTools(Path ticketsPath, EmbeddingModel embeddingModel) throws IOException {
        this.tickets = new ObjectMapper().readValue(ticketsPath.toFile(), new TypeReference<>() {});
        this.embeddingModel = embeddingModel;
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        for (SupportTicket ticket : tickets) {
            Document document = Document.from(ticket.asDocumentText());
            Embedding embedding = embeddingModel.embed(document.text()).content();
            embeddingStore.add(embedding, document);
        }
    }

    @Tool("Search for similar support tickets based on a problem description or question. Use for troubleshooting or how-to-fix questions.")
    public String searchSimilarTickets(String query) {
        List<EmbeddingMatch<Document>> results = embeddingStore.findRelevant(embeddingModel.embed(query).content(), 3);
        if (results.isEmpty()) {
            return "No similar tickets found.";
        }
        StringBuilder output = new StringBuilder("Found similar tickets:\n\n");
        for (int index = 0; index < results.size(); index++) {
            output.append("--- Ticket ").append(index + 1).append(" ---\n")
                    .append(results.get(index).embedded().text()).append("\n\n");
        }
        return output.toString();
    }

    @Tool("Retrieve the details of a specific support ticket by ID, such as TICK-001.")
    public String getTicketById(String ticketId) {
        String normalizedId = ticketId == null ? "" : ticketId.trim().toUpperCase();
        return tickets.stream()
                .filter(ticket -> ticket.ticketId().equals(normalizedId))
                .findFirst()
                .map(SupportTicket::asFullText)
                .orElse("Ticket " + normalizedId + " not found in the database.");
    }

    @Tool("Find all tickets in an exact category, such as Authentication, Database, or Payment.")
    public String searchByCategory(String category) {
        String normalizedCategory = category == null ? "" : category.trim();
        List<SupportTicket> matching = tickets.stream()
                .filter(ticket -> ticket.category().equalsIgnoreCase(normalizedCategory))
                .toList();
        if (matching.isEmpty()) {
            String available = tickets.stream().map(SupportTicket::category).distinct().sorted().collect(Collectors.joining(", "));
            return "No tickets found in category '" + normalizedCategory + "'. Available categories: " + available;
        }
        StringBuilder output = new StringBuilder("Found ").append(matching.size())
                .append(" tickets in category '").append(normalizedCategory).append("':\n\n");
        matching.forEach(ticket -> output.append("- [").append(ticket.ticketId()).append("] ")
                .append(ticket.title()).append(" (Priority: ").append(ticket.priority()).append(")\n"));
        return output.toString();
    }

    @Tool("Get statistics and an overview of the support-ticket database, including category and priority counts.")
    public String getTicketStatistics() {
        Map<String, Integer> categories = countBy(SupportTicket::category);
        Map<String, Integer> priorities = countBy(SupportTicket::priority);
        StringBuilder output = new StringBuilder("Ticket Database Statistics:\n")
                .append("Total Tickets: ").append(tickets.size()).append("\n\nBy Category:\n");
        appendSortedCounts(output, categories);
        output.append("\nBy Priority:\n");
        appendSortedCounts(output, priorities);
        return output.toString();
    }

    private Map<String, Integer> countBy(java.util.function.Function<SupportTicket, String> field) {
        Map<String, Integer> counts = new HashMap<>();
        tickets.forEach(ticket -> counts.merge(field.apply(ticket), 1, Integer::sum));
        return counts;
    }

    private void appendSortedCounts(StringBuilder output, Map<String, Integer> counts) {
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry -> output.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
    }
}
