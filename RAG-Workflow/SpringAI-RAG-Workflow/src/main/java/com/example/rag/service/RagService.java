package com.example.rag.service;

import com.example.rag.model.RagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private static final String FALLBACK = "I don't have enough relevant information in the ticket history to answer that question confidently.";
    private static final String PROMPT = """
            You are SupportDesk AI, a technical support assistant that helps engineers troubleshoot issues using historical support ticket data.

            CRITICAL RULES:
            1. Answer using ONLY information from the provided context.
            2. If the question is broad, provide the best matching known issues and state assumptions.
            3. If context is partially relevant, provide the most likely guidance from relevant tickets.
            4. If the answer is not present, say: I don't have enough information in the ticket history to answer that question.
            5. Do not make up information or use external knowledge.
            6. Always cite ticket IDs for every issue or resolution you mention.
            7. If multiple tickets are relevant, summarize each briefly.

            Context from support tickets:
            %s

            Question: %s

            Helpful Answer (with ticket citations):
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final TicketIngestionService ingestionService;
    private final int topK;
    private final double minimumSimilarity;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
                      TicketIngestionService ingestionService,
                      @Value("${rag.retrieval.top-k:3}") int topK,
                      @Value("${rag.retrieval.minimum-similarity:0.5}") double minimumSimilarity) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.ingestionService = ingestionService;
        this.topK = topK;
        this.minimumSimilarity = minimumSimilarity;
    }

    public RagResponse answer(String question) {
        ingestionService.initializeIfNeeded();
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(minimumSimilarity)
                .build());
        if (documents.isEmpty()) {
            return new RagResponse(FALLBACK, List.of());
        }

        String context = documents.stream()
                .map(document -> "---\n" + document.getText() + "\n---")
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        String answer = chatClient.prompt()
                .user(user -> user.text(PROMPT.formatted(context, question)))
                .call()
                .content();
        List<String> sources = documents.stream()
                .map(document -> String.valueOf(document.getMetadata().get("ticket_id")))
                .toList();
        return new RagResponse(answer, sources);
    }
}
