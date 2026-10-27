package com.example.rag.service;

import com.example.rag.model.SupportTicket;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class TicketIngestionService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private volatile boolean initialized;
    private volatile int ticketCount;

    public TicketIngestionService(VectorStore vectorStore, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.vectorStore = vectorStore;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public void initializeIfNeeded() {
        if (initialized) {
            return;
        }
        loadTickets();
    }

    public int loadTickets() {
        if (initialized) {
            return ticketCount;
        }
        synchronized (this) {
            if (initialized) {
                return ticketCount;
            }
            Resource resource = resourceLoader.getResource("classpath:synthetic_tickets.json");
            try {
                List<SupportTicket> tickets = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
                List<Document> documents = tickets.stream()
                        .map(ticket -> new Document(ticket.asDocumentText(), Map.of(
                                "ticket_id", ticket.ticket_id(),
                                "title", ticket.title(),
                                "category", ticket.category(),
                                "priority", ticket.priority(),
                                "source", "Ticket " + ticket.ticket_id())))
                        .toList();
                vectorStore.add(documents);
                ticketCount = documents.size();
                initialized = true;
                return ticketCount;
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load support tickets", exception);
            }
        }
    }
}
