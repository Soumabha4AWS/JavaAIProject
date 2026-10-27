package com.example.agenticrag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupportAssistantService {

    private static final String SYSTEM_PROMPT = """
            You are an expert support desk assistant. Use the support-ticket tools to troubleshoot issues,
            retrieve ticket details, filter by category, and summarize the ticket database.
            Always search similar tickets for troubleshooting or how-to-fix questions.
            Reference ticket IDs when giving solutions, be concise but thorough, and say when information is unavailable.
            You may call multiple tools when needed to produce a complete answer.
            """;

    private final ChatClient chatClient;
    private final SupportTicketTools tools;
    private final Map<String, String> lastQueries = new ConcurrentHashMap<>();

    public SupportAssistantService(ChatClient.Builder chatClientBuilder, SupportTicketTools tools) {
        this.tools = tools;
        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(20).build();
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(memoryAdvisor)
                .defaultTools(tools)
                .build();
    }

    public ChatResult ask(String conversationId, String query) {
        String previousQuery = lastQueries.get(conversationId);
        String rewrittenQuery = rewriteQuery(previousQuery, query);
        String answer = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(rewrittenQuery)
                .call()
                .content();
        lastQueries.put(conversationId, rewrittenQuery);
        return new ChatResult(answer, query, rewrittenQuery);
    }

    private String rewriteQuery(String previousQuery, String query) {
        if (previousQuery == null || previousQuery.isBlank()) {
            return query;
        }
        String rewritten = chatClient.prompt()
                .system("Rewrite the latest user question as one standalone support-ticket search query. "
                        + "Resolve references such as 'that' using the previous question. "
                        + "Return only the rewritten query, with no explanation.")
                .user("Previous question: %s%nLatest question: %s".formatted(previousQuery, query))
                .call()
                .content();
        return rewritten == null || rewritten.isBlank() ? query : rewritten.trim();
    }

    public int uploadTickets() throws java.io.IOException {
        return tools.loadTicketsFromResource();
    }

    public record ChatResult(String answer, String originalQuery, String queryRewrite) {
    }
}
