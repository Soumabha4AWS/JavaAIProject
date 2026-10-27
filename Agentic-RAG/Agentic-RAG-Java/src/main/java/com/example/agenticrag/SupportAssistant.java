package com.example.agenticrag;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.SystemMessage;

public interface SupportAssistant {
    @SystemMessage("""
            You are an expert support desk assistant helping troubleshoot technical issues.
            You have access to previous support tickets and their resolutions.
            Always search similar tickets for troubleshooting or how-to-fix questions.
            Reference ticket IDs when providing solutions, be concise but thorough, and admit
            when the database does not contain enough information. Use multiple tools when useful.
            Before a tool call, briefly explain the decision in one sentence beginning with 'Decision:'.
            """)
    String chat(@MemoryId String memoryId, @UserMessage String message);
}
