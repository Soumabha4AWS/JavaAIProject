package com.interviewkickstart.rag;

import java.util.ArrayList;
import java.util.List;

public final class Conversation {
    private final RagService rag;
    private final List<String> history = new ArrayList<>();

    public Conversation(RagService rag) {
        this.rag = rag;
    }

    public String ask(String question) {
        String standaloneQuestion = question;
        if (!history.isEmpty()) {
            standaloneQuestion = condense(question);
        }
        String context = RagPrompt.format(rag.retrieve(standaloneQuestion));
        String prompt = "You are SupportDesk AI. Answer using the ticket context and chat history.\n"
                + "Use history to resolve references such as 'that issue' or 'that ticket'.\n"
                + "Prioritize retrieved context for factual claims and cite ticket IDs when available.\n\n"
                + "Context:\n" + context + "\n\n"
                + "Chat history:\n" + String.join("\n", history) + "\n\n"
                + "User: " + question;
        String answer = rag.languageModel().generate(prompt);
        history.add("User: " + question);
        history.add("Assistant: " + answer);
        return answer;
    }

    private String condense(String question) {
        String prompt = "Given the chat history and a follow-up question, rephrase the follow-up as a standalone question "
                + "with all necessary context. If already standalone, return it unchanged. Output ONLY the question.\n\n"
                + String.join("\n", history) + "\n\nFollow-up: " + question;
        return rag.languageModel().generate(prompt).trim();
    }

    public List<String> history() {
        return List.copyOf(history);
    }
}