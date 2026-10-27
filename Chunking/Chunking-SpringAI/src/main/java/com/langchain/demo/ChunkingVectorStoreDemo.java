package com.langchain.demo;

import com.langchain.demo.model.SupportTicket;
import com.langchain.demo.service.ChunkingService;
import com.langchain.demo.service.VectorStoreService;
import com.langchain.demo.util.DataLoader;
import org.springframework.ai.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot demonstration of Spring AI chunking and vector store capabilities
 */
@SpringBootApplication
public class ChunkingVectorStoreDemo implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ChunkingVectorStoreDemo.class);

    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;

    public ChunkingVectorStoreDemo(ChunkingService chunkingService, VectorStoreService vectorStoreService) {
        this.chunkingService = chunkingService;
        this.vectorStoreService = vectorStoreService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ChunkingVectorStoreDemo.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=".repeat(80));
        logger.info("MODULE 2: CHUNKING & VECTOR STORES (Java/Spring Boot/Spring AI)");
        logger.info("=".repeat(80));

        // Step 1: Load data
        logger.info("\n### STEP 1: Loading Support Tickets ###\n");
        List<SupportTicket> tickets = loadTickets();

        // Step 2: Convert to Documents
        logger.info("\n### STEP 2: Converting Tickets to Documents ###\n");
        List<Document> documents = convertToDocuments(tickets);
        logger.info("✓ Created {} documents\n", documents.size());

        // Step 3: Demonstrate chunking strategies
        logger.info("\n### STEP 3: Chunking Strategies ###\n");
        demonstrateChunkingStrategies(documents);

        // Step 4: Vector store and retrieval
        logger.info("\n### STEP 4: Vector Store & Retrieval ###\n");
        demonstrateVectorStoreAndRetrieval(documents);

        // Step 5: Metadata filtering
        logger.info("\n### STEP 5: Metadata Filtering ###\n");
        demonstrateMetadataFiltering(documents);

        logger.info("\n" + "=".repeat(80));
        logger.info("DEMO COMPLETE!");
        logger.info("=".repeat(80));
    }

    /**
     * Load support tickets from the JSON file
     */
    private List<SupportTicket> loadTickets() throws IOException {
        String filePath = "./synthetic_tickets.json";

        try {
            List<SupportTicket> tickets = DataLoader.loadTickets(filePath);
            logger.info("✓ Loaded {} support tickets", tickets.size());
            return tickets;
        } catch (IOException e) {
            logger.error("Error loading tickets: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Convert support tickets to Spring AI Document objects
     */
    private List<Document> convertToDocuments(List<SupportTicket> tickets) {
        return tickets.stream()
                .map(ticket -> {
                    String content = DataLoader.formatTicketAsText(ticket);

                    // Create metadata Map for Spring AI
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("ticket_id", ticket.getTicketId());
                    metadata.put("category", ticket.getCategory());
                    metadata.put("priority", ticket.getPriority());
                    metadata.put("title", ticket.getTitle());

                    return new Document(content, metadata);
                })
                .toList();
    }

    /**
     * Demonstrate different chunking strategies
     */
    private void demonstrateChunkingStrategies(List<Document> documents) {
        // Strategy 1: Fixed-size chunking
        logger.info("--- Strategy 1: Fixed-Size Chunking ---");
        List<Document> fixedChunks = chunkingService.fixedSizeChunking(documents, 200, 20);
        chunkingService.reportChunkingStats(documents, fixedChunks, "Fixed-Size");

        // Strategy 2: Recursive chunking (recommended)
        logger.info("\n--- Strategy 2: Recursive Character Splitting (RECOMMENDED) ---");
        List<Document> recursiveChunks = chunkingService.recursiveChunking(documents, 300, 50);
        chunkingService.reportChunkingStats(documents, recursiveChunks, "Recursive");

        // Strategy 3: Whole documents (no chunking)
        logger.info("\n--- Strategy 3: Whole Documents (No Chunking) ---");
        List<Document> wholeDocChunks = chunkingService.wholeDocumentChunking(documents);
        chunkingService.reportChunkingStats(documents, wholeDocChunks, "Whole Documents");

        logger.info("\n✓ Chunking strategies demonstrated successfully");
    }

    /**
     * Demonstrate vector store operations and retrieval
     */
    private void demonstrateVectorStoreAndRetrieval(List<Document> documents) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("dummy-key")) {
            logger.warn("OPENAI_API_KEY environment variable not set");
            logger.warn("Set it before running: export OPENAI_API_KEY='your-key'");
            logger.info("Skipping vector store operations");
            return;
        }

        try {
            // Add documents to vector store service
            vectorStoreService.clear();
            vectorStoreService.addDocuments(documents);

            // Perform similarity search
            String query = "Authentication problems after password reset";
            logger.info("\nSearching for: '{}'", query);

            List<VectorStoreService.StoredDocument> results = vectorStoreService.similaritySearch(query, 3);
            logger.info("\n✓ Top {} results:", results.size());
            for (int i = 0; i < results.size(); i++) {
                VectorStoreService.StoredDocument doc = results.get(i);
                logger.info("\n#{}  [Ticket: {}]", i + 1, doc.metadata.get("ticket_id"));
                logger.info("    Category: {}", doc.metadata.get("category"));
                logger.info("    Priority: {}", doc.metadata.get("priority"));
            }
        } catch (Exception e) {
            logger.error("Error with embedding model: {}", e.getMessage(), e);
            logger.info("Continuing without embedding-based search...");
        }
    }

    /**
     * Demonstrate metadata filtering in vector search
     */
    private void demonstrateMetadataFiltering(List<Document> documents) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("dummy-key")) {
            logger.info("Skipping metadata filtering operations");
            return;
        }

        try {
            // Example 1: Filter by category
            logger.info("\n--- Filtering by Category: 'Authentication' ---");
            String query1 = "Authentication problems after password reset";
            List<VectorStoreService.StoredDocument> filteredResults = vectorStoreService.similaritySearchWithFilter(
                    query1, 3, "category", "Authentication");

            logger.info("\n✓ Filtered results: {} documents found", filteredResults.size());
            for (int i = 0; i < filteredResults.size(); i++) {
                VectorStoreService.StoredDocument doc = filteredResults.get(i);
                logger.info("#{}  [{}] {}", i + 1, doc.metadata.get("ticket_id"), doc.metadata.get("title"));
            }

            // Example 2: Filter by priority
            logger.info("\n--- Filtering by Priority: 'High' ---");
            String query2 = "Database performance issues";
            List<VectorStoreService.StoredDocument> highPriorityResults = vectorStoreService.similaritySearchWithFilter(
                    query2, 3, "priority", "High");

            logger.info("\n✓ High-priority results: {} documents found", highPriorityResults.size());
            for (int i = 0; i < highPriorityResults.size(); i++) {
                VectorStoreService.StoredDocument doc = highPriorityResults.get(i);
                logger.info("#{}  [{}] {} (Priority: {})", i + 1, doc.metadata.get("ticket_id"),
                        doc.metadata.get("title"), doc.metadata.get("priority"));
            }
        } catch (Exception e) {
            logger.error("Error with embedding model: {}", e.getMessage(), e);
            logger.info("Continuing without embedding-based search...");
        }
    }
}
