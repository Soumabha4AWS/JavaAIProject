package com.openai;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OpenAIConnectModule {
    private static final String API_KEY = "";
    private static final String MODEL = "gpt-4o-mini";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static CompletableFuture<String> queryOpenAI(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("\n--- Querying OpenAI (GPT-4o-mini) ---");

            try {
                ObjectMapper mapper = new ObjectMapper();
                
                ArrayNode messagesArray = mapper.createArrayNode();
                ObjectNode systemMessage = mapper.createObjectNode();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are a helpful assistant.");
                messagesArray.add(systemMessage);
                
                ObjectNode userMessage = mapper.createObjectNode();
                userMessage.put("role", "user");
                userMessage.put("content", prompt);
                messagesArray.add(userMessage);
                
                ObjectNode requestBody = mapper.createObjectNode();
                requestBody.put("model", MODEL);
                requestBody.set("messages", messagesArray);
                requestBody.put("temperature", 0.7);
                
                String jsonInput = mapper.writeValueAsString(requestBody);
                
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setDoOutput(true);
                
                connection.getOutputStream().write(jsonInput.getBytes());
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    String response = new String(connection.getInputStream().readAllBytes());
                    ObjectNode responseJson = mapper.readValue(response, ObjectNode.class);
                    return responseJson.get("choices").get(0).get("message").get("content").asText();
                } else {
                    return "OpenAI Error: HTTP " + responseCode;
                }
            } catch (Exception error) {
                return "OpenAI Error: " + error.getMessage();
            }
        });
    }
}
