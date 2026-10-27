package com.indexing.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Configuration manager for LlamaIndex Java application.
 * Reads all settings (including the OpenAI API key) from application.yml
 * under the "openai" prefix. Spring Boot manages the lifecycle of this bean.
 */
@Component
@ConfigurationProperties(prefix = "openai")
public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private String apiKey;
    private String embeddingModel;
    private String llmModel;
    private long embeddingTimeout;
    private long llmTimeout;
    private int maxRetries;
    private int httpxTimeout;

    @PostConstruct
    public void validate() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "openai.api-key must be set in application.yml or via the OPENAI_API_KEY environment variable.");
        }
        logger.info("Configuration initialized successfully");
        logger.info("Embedding Model: {}", embeddingModel);
        logger.info("LLM Model: {}", llmModel);
    }

    // Getters
    public String getOpenaiApiKey() {
        return apiKey;
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

    // Setters — required by @ConfigurationProperties for binding
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void setLlmModel(String llmModel) {
        this.llmModel = llmModel;
    }

    public void setEmbeddingTimeout(long embeddingTimeout) {
        this.embeddingTimeout = embeddingTimeout;
    }

    public void setLlmTimeout(long llmTimeout) {
        this.llmTimeout = llmTimeout;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setHttpxTimeout(int httpxTimeout) {
        this.httpxTimeout = httpxTimeout;
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

