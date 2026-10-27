package com.interviewkickstart.rag;

import java.util.List;
import java.util.stream.Collectors;

public final class RagPrompt {
    private RagPrompt() {
    }

    public static String forQuestion(String context, String question) {
        return "You are SupportDesk AI, a technical support assistant that helps engineers troubleshoot issues using historical support ticket data.\n\n"
                + "CRITICAL RULES:\n"
                + "1. Answer using ONLY information from the provided context.\n"
                + "2. If the question is broad or underspecified, provide the best matching known issue(s) from context and state any assumptions.\n"
                + "3. If context is partially relevant, still provide the most likely troubleshooting guidance from relevant tickets.\n"
                + "4. If the answer is truly not present in context, say \"I don't have enough information in the ticket history to answer that question.\"\n"
                + "5. DO NOT make up information or use external knowledge.\n"
                + "6. Always cite ticket IDs for every issue/resolution you mention.\n"
                + "7. If multiple tickets are relevant, summarize each briefly.\n\n"
                + "Context from support tickets:\n" + context + "\n\n"
                + "Question: " + question + "\n\n"
                + "Helpful Answer (with ticket citations):";
    }

    public static String format(List<TicketRetriever.ScoredTicket> documents) {
        return documents.stream()
                .map(result -> "---\n" + result.ticket().documentText() + "\n---")
                .collect(Collectors.joining("\n"));
    }
}