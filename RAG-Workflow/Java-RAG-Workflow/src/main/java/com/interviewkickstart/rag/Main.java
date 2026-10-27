package com.interviewkickstart.rag;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import java.nio.file.Path;
import java.util.List;

public final class Main {


    public static void main(String[] args) throws Exception {        
        String apiKey = "";
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set OPENAI_API_KEY before running the RAG demo.");
        }


        List<SupportTicket> tickets = TicketLoader.load(Path.of("synthetic_tickets.json"));
        System.out.println("Tickets: " + tickets);

        EmbeddingModel embeddings = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();
        System.out.println("Embeddings Model Created" + embeddings);

        var llm = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .build();
        System.out.println("LLM Created: " + llm);

        RagService rag = new RagService(new TicketRetriever(tickets, embeddings), llm, 3);
        System.out.println("RAG Service Created: " + rag);

        List<String> questions = List.of(
                "How do I fix authentication failures after password reset?",
                "What causes database connection timeouts?",
                "Why are emails not being delivered?",
                "How do I make the perfect pizza?");
        for (String question : questions) {
            System.out.println("\nQUESTION: " + question);
            System.out.println("ANSWER: " + rag.answer(question));
        }

        System.out.println("\nVALIDATED ANSWER: "
                + rag.answerWithValidation("What is the capital of France?", 0.5));

        Conversation conversation = new Conversation(rag);
        System.out.println("\nMULTI-TURN: "
                + conversation.ask("How do I fix authentication failures after a password reset?"));
        System.out.println("\nMULTI-TURN: " + conversation.ask("What was the ticket ID for that issue?"));
        System.out.println("\nMULTI-TURN: " + conversation.ask("What was the resolution for that ticket?"));
    }
}