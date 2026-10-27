package com.serverstatus.service;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for orchestrating the AI agent
 */
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_KEY = "";

    /**
     * Main agent loop that processes user issues
     */
    public static void runAgent(String userIssue) throws Exception {
        logger.info("\n--- New Incident: {} ---", userIssue);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(),
                "You are a Level 1 IT Responder. Investigate server issues. " +
                "If CPU or Memory is > 90%, restart the service. If logs show critical dependency errors " +
                "(like connection refused) that a restart won't fix, escalate to an engineer."));

        messages.add(new ChatMessage(ChatMessageRole.USER.value(), userIssue));

        OpenAiService service = new OpenAiService(API_KEY);

        while (true) {
            logger.info("\n[AI Thinking...]");

            try {
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model("gpt-4o")
                        .messages(messages)
                        .build();

                ChatCompletionResult response = service.createChatCompletion(request);
                String responseContent = response.getChoices().get(0).getMessage().getContent();
                
                messages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), responseContent));

                // For this simplified version, we just output the final response
                // In production, you would parse tool_calls if the API response includes them
                logger.info("\n[FINAL RESPONSE]: {}", responseContent);
                break;

            } catch (Exception error) {
                logger.error("Error in agent loop: {}", error.getMessage());
                break;
            }
        }
    }
}
