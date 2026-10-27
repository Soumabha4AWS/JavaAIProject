package com.serverstatus.service;

import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for connecting to OpenAI API
 */
public class OpenAIService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    private static final String API_KEY = "";

    /**
     * Creates and returns an OpenAI service instance
     * @return OpenAI service
     */
    public static OpenAiService createService() {
        logger.info("\n--- Querying OpenAI (GPT-4o) ---");
        
        try {
            return new OpenAiService(API_KEY);
        } catch (Exception error) {
            throw new RuntimeException("OpenAI Error: " + error.getMessage(), error);
        }
    }
}
