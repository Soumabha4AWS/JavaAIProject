package com.weather.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class WeatherAgentService {

    private final ChatClient chatClient;

    public WeatherAgentService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    public String runWeatherAgent(String userPrompt) {
        System.out.println("\n--- Running Getting Weather Report Agent ---");

        String systemPrompt = "You are a Weather Report Agent. Help users find weather information for their locations.";

        try {
            String response = this.chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .functions("getCurrentWeather")
                    .call()
                    .content();

            System.out.println("\n--- FINAL AGENT SUMMARY ---");
            System.out.println(response);

            return response;
        } catch (Exception e) {
            System.err.println("Error running weather agent: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

}
