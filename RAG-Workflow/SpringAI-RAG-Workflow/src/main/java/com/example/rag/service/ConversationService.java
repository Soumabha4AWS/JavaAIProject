package com.example.rag.service;

import com.example.rag.model.RagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private static final String FALLBACK = "I don't have enough information in the ticket history to answer that question.";
    private static final String PROMPT = """
            You are SupportDesk AI. Answer using the ticket context and conversation history below.
            Use history to resolve references such as 'that issue' or 'that ticket'.
            Prioritize retrieved context for factual claims. If information is unavailable, say: %s
            Always cite ticket IDs when available.

            Context:
            %s
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final TicketIngestionService ingestionService;
    private final int topK;
    private final double minimumSimilarity;
    private final ConcurrentHashMap<String, List<Message>> conversations = new ConcurrentHashMap<>();

    public ConversationService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
                               TicketIngestionService ingestionService,
                               @Value("${rag.retrieval.top-k:3}") int topK,
                               @Value("${rag.retrieval.minimum-similarity:0.5}") double minimumSimilarity) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.ingestionService = ingestionService;
        this.topK = topK;
        this.minimumSimilarity = minimumSimilarity;
    }

    public RagResponse answer(String conversationId, String question) {
        ingestionService.initializeIfNeeded();
        List<Message> history = conversations.computeIfAbsent(conversationId, ignored -> new java.util.concurrent.CopyOnWriteArrayList<>());
        String searchQuestion = question;
        if (!history.isEmpty()) {
            searchQuestion = chatClient.prompt()
                    .messages(history)
                    .user(user -> user.text("Rewrite the following follow-up as a standalone search question. Output only the rewritten question: " + question))
                    .call()
                    .content();
        }
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(searchQuestion)
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
                .messages(history)
                .user(user -> user.text(PROMPT.formatted(FALLBACK, context) + "\nCurrent question: " + question))
                .call()
                .content();
        history.add(new UserMessage(question));
        history.add(new AssistantMessage(answer));
        List<String> sources = documents.stream()
                .map(document -> String.valueOf(document.getMetadata().get("ticket_id")))
                .toList();
        return new RagResponse(answer, sources);
    }
}
