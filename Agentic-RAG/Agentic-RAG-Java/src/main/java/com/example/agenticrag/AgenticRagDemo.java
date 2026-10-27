package com.example.agenticrag;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;

import java.nio.file.Path;

public final class AgenticRagDemo {
    private AgenticRagDemo() {
    }

    public static void main(String[] args) throws Exception {
        String apiKey = "";
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set OPENAI_API_KEY before running the demo.");
        }

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();
        SupportTicketTools tools = new SupportTicketTools(Path.of("synthetic_tickets.json"), embeddingModel);

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .build();
        SupportAssistant assistant = AiServices.builder(SupportAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(tools)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();

        runExamples(assistant);
    }

    private static void runExamples(SupportAssistant assistant) {
        run("Troubleshooting", assistant, "demo", "How do I fix authentication problems after password reset?");
        run("Ticket lookup", assistant, "demo", "Show me details of ticket TICK-005");
        run("Category search", assistant, "demo", "What payment-related issues have we seen?");
        run("Statistics", assistant, "demo", "Give me an overview of the ticket database");
        run("Multi-step reasoning", assistant, "demo", "Find database-related critical issues and tell me how they were resolved");

        System.out.println("\n=== Conversational memory ===");
        run("Turn 1", assistant, "conversation", "What issues have we had with iOS?");
        run("Turn 2", assistant, "conversation", "What was the ticket ID for that?");
        run("Turn 3", assistant, "conversation", "How was it resolved?");
    }

    private static void run(String title, SupportAssistant assistant, String memoryId, String query) {
        System.out.println("\n=== " + title + " ===");
        System.out.println("User: " + query);
        System.out.println("Assistant: " + assistant.chat(memoryId, query));
    }
}
