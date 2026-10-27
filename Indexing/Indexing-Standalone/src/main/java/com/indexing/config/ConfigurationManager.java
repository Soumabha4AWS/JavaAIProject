package com.indexing.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration manager for LlamaIndex Java application.
 * Mirrors Python config.py - loads environment variables and initializes API clients.
 */
public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    
    private static ConfigurationManager instance;

    private String openaiApiKey;
    private String embeddingModel;
    private String llmModel;
    private long embeddingTimeout;
    private long llmTimeout;
    private int maxRetries;
    private int httpxTimeout;

    private ConfigurationManager() {
        this.openaiApiKey = "";
        
        // Fall back to hardcoded key for testing (should be env var in production)
        if (this.openaiApiKey == null || this.openaiApiKey.trim().isEmpty()) {
            this.openaiApiKey = System.getProperty("openai.api.key");
            logger.warn("OPENAI_API_KEY not found in environment variables, using system property");
        }
        
        if (this.openaiApiKey == null || this.openaiApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "OPENAI_API_KEY must be set in environment variables or via -Dopenai.api.key");
        }

        // Initialize settings (matching Python config.py)
        this.embeddingModel = "text-embedding-3-small";
        this.llmModel = "gpt-4o-mini";
        this.embeddingTimeout = 120_000; // 2 minutes in ms
        this.llmTimeout = 300_000; // 5 minutes in ms
        this.maxRetries = 5;
        this.httpxTimeout = 300_000; // 5 minutes in ms

        logger.info("Configuration initialized successfully");
        logger.info("Embedding Model: {}", embeddingModel);
        logger.info("LLM Model: {}", llmModel);
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    // Getters
    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public String getLlmModel() {
        return llmModel;
    }

    public long getEmbeddingTimeout() {
        return embeddingTimeout;
    }

    public long getLlmTimeout() {
        return llmTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getHttpxTimeout() {
        return httpxTimeout;
    }

    // Setters for testing
    public void setOpenaiApiKey(String apiKey) {
        this.openaiApiKey = apiKey;
    }

    public void setEmbeddingModel(String model) {
        this.embeddingModel = model;
    }

    public void setLlmModel(String model) {
        this.llmModel = model;
    }

    public void setEmbeddingTimeout(long timeoutMs) {
        this.embeddingTimeout = timeoutMs;
    }

    public void setLlmTimeout(long timeoutMs) {
        this.llmTimeout = timeoutMs;
    }

    @Override
    public String toString() {
        return "ConfigurationManager{" +
                "embeddingModel='" + embeddingModel + '\'' +
                ", llmModel='" + llmModel + '\'' +
                ", embeddingTimeout=" + embeddingTimeout + "ms" +
                ", llmTimeout=" + llmTimeout + "ms" +
                ", maxRetries=" + maxRetries +
                '}';
    }
}
