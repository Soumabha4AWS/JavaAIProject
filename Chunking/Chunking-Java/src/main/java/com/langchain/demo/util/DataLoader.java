package com.langchain.demo.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langchain.demo.model.SupportTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility class for loading support ticket data from JSON file
 */
public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Load support tickets from JSON file
     *
     * @param filePath Path to the JSON file containing support tickets
     * @return List of SupportTicket objects
     * @throws IOException If file cannot be read
     */
    public static List<SupportTicket> loadTickets(String filePath) throws IOException {
        logger.info("Loading tickets from: {}", filePath);
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        String jsonContent = Files.readString(path);
        List<SupportTicket> tickets = objectMapper.readValue(
                jsonContent,
                new TypeReference<List<SupportTicket>>() {}
        );

        logger.info("Successfully loaded {} tickets", tickets.size());
        return tickets;
    }

    /**
     * Format ticket into readable text for embedding
     *
     * @param ticket The support ticket to format
     * @return Formatted text representation of the ticket
     */
    public static String formatTicketAsText(SupportTicket ticket) {
        return String.format("""
                Ticket ID: %s
                Title: %s
                Category: %s
                Priority: %s
                Description: %s
                Resolution: %s
                """,
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getDescription(),
                ticket.getResolution()
        );
    }
}
