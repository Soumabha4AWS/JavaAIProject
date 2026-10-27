package com.indexing.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indexing.model.Document;
import com.indexing.model.SupportTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Data loader that converts JSON tickets into LlamaIndex Document format.
 * Mirrors Python config.py's document loading and preprocessing.
 */
public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loads support tickets from JSON file and converts to Documents.
     * @param jsonFilePath path to synthetic_tickets.json
     * @return list of Document objects with enriched content and metadata
     * @throws IOException if file cannot be read
     */
    public static List<Document> loadDocuments(String jsonFilePath) throws IOException {
        logger.info("Loading support tickets from: {}", jsonFilePath);
        
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new IOException("File not found: " + jsonFilePath);
        }

        // Parse JSON array of tickets
        List<SupportTicket> tickets = objectMapper.readValue(
            jsonFile,
            new TypeReference<List<SupportTicket>>() {}
        );

        logger.info("Loaded {} support tickets", tickets.size());

        // Convert tickets to Documents with rich content
        List<Document> documents = new ArrayList<>();
        for (SupportTicket ticket : tickets) {
            // Combine all fields into content (matching Python config.py)
            String content = ticket.toRichContent();

            // Create metadata map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ticket_id", ticket.getTicketId());
            metadata.put("category", ticket.getCategory());
            metadata.put("priority", ticket.getPriority());
            metadata.put("title", ticket.getTitle());
            metadata.put("created_date", ticket.getCreatedDate());
            metadata.put("resolved_date", ticket.getResolvedDate());

            Document doc = new Document(content, metadata);
            documents.add(doc);
        }

        logger.info("✓ Converted {} tickets to Documents", documents.size());
        return documents;
    }

    /**
     * Loads documents from JSON file, with custom path resolution.
     * @param baseDir base directory to search from
     * @param filename filename relative to baseDir
     * @return list of Document objects
     * @throws IOException if file cannot be read
     */
    public static List<Document> loadDocuments(String baseDir, String filename) throws IOException {
        String fullPath = new File(baseDir, filename).getAbsolutePath();
        return loadDocuments(fullPath);
    }

    /**
     * Attempts to find and load synthetic_tickets.json from common locations.
     * @return list of Document objects
     * @throws IOException if file cannot be found
     */
    public static List<Document> loadDocumentsFromDefaultLocation() throws IOException {
        // Try multiple locations
        String[] paths = {
            "./synthetic_tickets.json",
            "../synthetic_tickets.json",
            "../../synthetic_tickets.json",
            System.getProperty("user.dir") + "/synthetic_tickets.json"
        };

        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                logger.info("Found synthetic_tickets.json at: {}", file.getAbsolutePath());
                return loadDocuments(file.getAbsolutePath());
            }
        }

        throw new IOException(
            "Could not find synthetic_tickets.json in any standard location. " +
            "Searched: " + Arrays.toString(paths)
        );
    }
}
