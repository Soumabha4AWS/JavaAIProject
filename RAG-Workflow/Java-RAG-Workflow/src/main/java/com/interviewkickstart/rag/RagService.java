package com.interviewkickstart.rag;

import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.List;

public final class RagService {
    private static final String LOW_CONFIDENCE =
            "I don't have enough relevant information in the ticket history to answer that question confidently.";

    private final TicketRetriever retriever;
    private final ChatLanguageModel llm;
    private final int topK;

    public RagService(TicketRetriever retriever, ChatLanguageModel llm, int topK) {
        this.retriever = retriever;
        this.llm = llm;
        this.topK = topK;
    }

    public String answer(String question) {
        List<TicketRetriever.ScoredTicket> documents = retriever.search(question, topK);
        return llm.generate(RagPrompt.forQuestion(RagPrompt.format(documents), question));
    }

    public String answerWithValidation(String question, double minimumSimilarity) {
        List<TicketRetriever.ScoredTicket> documents = retriever.search(question, topK);
        if (documents.isEmpty() || documents.get(0).score() < minimumSimilarity) {
            return LOW_CONFIDENCE;
        }
        return llm.generate(RagPrompt.forQuestion(RagPrompt.format(documents), question));
    }

    public List<TicketRetriever.ScoredTicket> retrieve(String question) {
        return retriever.search(question, topK);
    }

    public ChatLanguageModel languageModel() {
        return llm;
    }
}