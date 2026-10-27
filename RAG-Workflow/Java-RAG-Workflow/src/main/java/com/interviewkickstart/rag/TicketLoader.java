package com.interviewkickstart.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TicketLoader {
    private TicketLoader() {
    }

    public static List<SupportTicket> load(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream input = Files.newInputStream(path)) {
            return mapper.readValue(input, new TypeReference<>() {
            });
        }
    }
}