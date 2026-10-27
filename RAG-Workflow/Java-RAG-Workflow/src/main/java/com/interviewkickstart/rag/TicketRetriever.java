package com.interviewkickstart.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public final class TicketRetriever {
    private final List<SupportTicket> tickets;
    private final List<Embedding> embeddings;
    private final EmbeddingModel embeddingModel;

    public TicketRetriever(List<SupportTicket> tickets, EmbeddingModel embeddingModel) {
        this.tickets = List.copyOf(tickets);
        this.embeddingModel = embeddingModel;
        this.embeddings = tickets.stream()
                .map(ticket -> embeddingModel.embed(ticket.documentText()).content())
                .toList();
    }

    public List<ScoredTicket> search(String query, int limit) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return IntStream.range(0, tickets.size())
                .mapToObj(index -> new ScoredTicket(tickets.get(index),
                        cosineSimilarity(queryEmbedding, embeddings.get(index))))
                .sorted(Comparator.comparingDouble(ScoredTicket::score).reversed())
                .limit(limit)
                .toList();
    }

    private static double cosineSimilarity(Embedding left, Embedding right) {
        float[] a = left.vector();
        float[] b = right.vector();
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int index = 0; index < a.length; index++) {
            dot += a[index] * b[index];
            leftNorm += a[index] * a[index];
            rightNorm += b[index] * b[index];
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    public record ScoredTicket(SupportTicket ticket, double score) {
    }
}