package com.indexing.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indexing.config.ConfigurationManager;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * OpenAI API client for embeddings and LLM calls.
 * Handles API communication, retries, and timeout configuration.
 */
public class OpenAIClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);
    
    private static final String OPENAI_BASE_URL = "https://api.openai.com/v1";
    private static final String EMBEDDING_ENDPOINT = "/embeddings";
    private static final String CHAT_ENDPOINT = "/chat/completions";

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int maxRetries;

    public OpenAIClient(ConfigurationManager config) {
        this.apiKey = config.getOpenaiApiKey();
        this.maxRetries = config.getMaxRetries();
        this.objectMapper = new ObjectMapper();

        // Configure OkHttp client with timeouts
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(config.getLlmTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(config.getLlmTimeout(), TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build();

        logger.info("OpenAI client initialized with model: {}", config.getLlmModel());
    }

    /**
     * Generates embedding vector for input text using OpenAI API.
     * @param text text to embed
     * @param model embedding model name
     * @return embedding vector as array of doubles
     * @throws IOException if API call fails
     */
    public double[] getEmbedding(String text, String model) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", text);

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                String response = makeApiCall(EMBEDDING_ENDPOINT, requestBody);
                JsonNode jsonNode = objectMapper.readTree(response);

                // Extract embedding from response
                JsonNode data = jsonNode.get("data");
                if (data != null && data.isArray() && data.size() > 0) {
                    JsonNode embeddingNode = data.get(0).get("embedding");
                    if (embeddingNode != null && embeddingNode.isArray()) {
                        double[] embedding = new double[embeddingNode.size()];
                        for (int i = 0; i < embeddingNode.size(); i++) {
                            embedding[i] = embeddingNode.get(i).asDouble();
                        }
                        return embedding;
                    }
                }

                throw new IOException("Invalid embedding response format");

            } catch (IOException e) {
                if (attempt < maxRetries - 1) {
                    long backoffMs = (long) Math.pow(2, attempt) * 1000;
                    logger.warn("Embedding API call failed, retrying in {}ms (attempt {}/{})", 
                               backoffMs, attempt + 1, maxRetries);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry backoff", ie);
                    }
                } else {
                    logger.error("Embedding API call failed after {} retries", maxRetries);
                    throw e;
                }
            }
        }

        throw new IOException("Failed to get embedding after " + maxRetries + " retries");
    }

    /**
     * Calls LLM to generate response based on prompt.
     * @param prompt prompt text
     * @param model LLM model name
     * @return LLM response text
     * @throws IOException if API call fails
     */
    public String callLLM(String prompt, String model) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                String response = makeApiCall(CHAT_ENDPOINT, requestBody);
                JsonNode jsonNode = objectMapper.readTree(response);

                // Extract response text
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    String content = choices.get(0).get("message").get("content").asText();
                    return content.trim();
                }

                throw new IOException("Invalid LLM response format");

            } catch (IOException e) {
                if (attempt < maxRetries - 1) {
                    long backoffMs = (long) Math.pow(2, attempt) * 1000;
                    logger.warn("LLM API call failed, retrying in {}ms (attempt {}/{})", 
                               backoffMs, attempt + 1, maxRetries);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry backoff", ie);
                    }
                } else {
                    logger.error("LLM API call failed after {} retries", maxRetries);
                    throw e;
                }
            }
        }

        throw new IOException("Failed to call LLM after " + maxRetries + " retries");
    }

    /**
     * Makes HTTP POST request to OpenAI API.
     * @param endpoint API endpoint path
     * @param requestBody request body as map
     * @return response body as string
     * @throws IOException if request fails
     */
    private String makeApiCall(String endpoint, Map<String, Object> requestBody) throws IOException {
        String url = OPENAI_BASE_URL + endpoint;
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("API request failed with status " + response.code() + ": " + errorBody);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }

            return body.string();
        }
    }

    /**
     * Extracts keywords from text using LLM.
     * @param text text to extract keywords from
     * @param model LLM model name
     * @return list of extracted keywords
     * @throws IOException if API call fails
     */
    public List<String> extractKeywords(String text, String model) throws IOException {
        String prompt = String.format(
            "Extract the most important 5-10 keywords from the following text. " +
            "Return them as a comma-separated list.\n\n%s",
            text
        );

        String response = callLLM(prompt, model);
        List<String> keywords = new ArrayList<>();

        for (String keyword : response.split(",")) {
            String cleaned = keyword.trim().toLowerCase();
            if (!cleaned.isEmpty()) {
                keywords.add(cleaned);
            }
        }

        return keywords;
    }
}
