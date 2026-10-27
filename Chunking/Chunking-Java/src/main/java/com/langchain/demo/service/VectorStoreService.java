package com.langchain.demo.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for demonstrating vector store operations
 * Note: This is a simplified in-memory implementation for demonstration purposes
 * In production, use a proper vector database like Chroma, Pinecone, or Weaviate
 */
public class VectorStoreService {
    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private final EmbeddingModel embeddingModel;
    private final Map<String, StoredDocument> store;

    public static class StoredDocument {
        public String id;
        public String content;
        public Embedding embedding;
        public Map<String, String> metadata;

        public StoredDocument(String id, String content, Embedding embedding, Map<String, String> metadata) {
            this.id = id;
            this.content = content;
            this.embedding = embedding;
            this.metadata = metadata;
        }
    }

    public VectorStoreService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        this.store = new HashMap<>();
    }

    /**
     * Add documents to the vector store
     *
     * @param documents List of documents to add
     */
    public void addDocuments(List<Document> documents) {
        logger.info("Adding {} documents to vector store", documents.size());

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String docId = "doc_" + i;

            // Generate embedding for the document
            Embedding embedding = embeddingModel.embed(doc.text()).content();

            // Extract metadata
            Map<String, String> metadata = new HashMap<>(doc.metadata().asMap());

            StoredDocument storedDoc = new StoredDocument(docId, doc.text(), embedding, metadata);
            store.put(docId, storedDoc);

            logger.debug("Added document {} with {} metadata fields", docId, metadata.size());
        }

        logger.info("Vector store now contains {} documents", store.size());
    }

    /**
     * Perform similarity search
     *
     * @param query Search query string
     * @param k Number of top results to return
     * @return List of matching documents sorted by similarity
     */
    public List<StoredDocument> similaritySearch(String query, int k) {
        logger.info("Performing similarity search: '{}'", query);

        // Generate embedding for the query
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // Calculate similarity scores for all documents
        return store.values().stream()
                .map(doc -> {
                    double similarity = cosineSimilarity(queryEmbedding.vector(), doc.embedding.vector());
                    return new AbstractMap.SimpleEntry<>(doc, similarity);
                })
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(k)
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
    }

    /**
     * Perform similarity search with metadata filtering
     *
     * @param query Search query string
     * @param k Number of top results to return
     * @param filterKey Metadata key to filter on
     * @param filterValue Metadata value to match
     * @return List of matching documents sorted by similarity
     */
    public List<StoredDocument> similaritySearchWithFilter(String query, int k, String filterKey, String filterValue) {
        logger.info("Performing filtered similarity search: '{}', filter: {}={}", query, filterKey, filterValue);

        // Generate embedding for the query
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // Calculate similarity scores for documents matching the filter
        return store.values().stream()
                .filter(doc -> filterValue.equals(doc.metadata.get(filterKey)))
                .map(doc -> {
                    double similarity = cosineSimilarity(queryEmbedding.vector(), doc.embedding.vector());
                    return new AbstractMap.SimpleEntry<>(doc, similarity);
                })
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(k)
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
    }

    /**
     * Calculate cosine similarity between two vectors
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Cosine similarity score (0-1)
     */
    private double cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            magnitude1 += v1[i] * v1[i];
            magnitude2 += v2[i] * v2[i];
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * Get the number of documents in the store
     *
     * @return Number of documents
     */
    public int getDocumentCount() {
        return store.size();
    }

    /**
     * Clear the vector store
     */
    public void clear() {
        store.clear();
        logger.info("Vector store cleared");
    }
}
