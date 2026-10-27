package com.indexing.index;

import com.indexing.model.Document;
import com.indexing.model.SearchResult;
import com.indexing.util.OpenAIClient;
import com.indexing.util.VectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Vector Index implementation using semantic similarity search.
 * Embeds all documents and retrieves by cosine similarity.
 * Mirrors Python VectorStoreIndex from llama_index.
 */
public class VectorIndex {
    private static final Logger logger = LoggerFactory.getLogger(VectorIndex.class);

    private final OpenAIClient openAIClient;
    private final String embeddingModel;
    private final Map<String, double[]> documentEmbeddings;
    private final Map<String, Document> documentStore;
    private final List<String> documentIds;

    public VectorIndex(OpenAIClient openAIClient, String embeddingModel) {
        this.openAIClient = openAIClient;
        this.embeddingModel = embeddingModel;
        this.documentEmbeddings = new HashMap<>();
        this.documentStore = new HashMap<>();
        this.documentIds = new ArrayList<>();
    }

    /**
     * Builds index from documents.
     * Creates embeddings for all documents.
     * @param documents documents to index
     * @throws IOException if embedding API calls fail
     */
    public void buildIndex(List<Document> documents) throws IOException {
        logger.info("Building Vector Index with {} documents...", documents.size());
        System.out.println("\nBuilding Vector Index from documents...");

        int processed = 0;
        for (Document doc : documents) {
            try {
                String docId = doc.getNodeId();
                documentStore.put(docId, doc);
                documentIds.add(docId);

                // Generate embedding
                double[] embedding = openAIClient.getEmbedding(doc.getText(), embeddingModel);
                documentEmbeddings.put(docId, embedding);

                processed++;
                if (processed % 5 == 0) {
                    System.out.println("  ✓ Processed " + processed + " documents...");
                }
            } catch (IOException e) {
                logger.error("Failed to embed document {}: {}", doc.getNodeId(), e.getMessage());
                throw e;
            }
        }

        logger.info("✓ Vector Index built successfully with {} documents", documentIds.size());
        System.out.println("✓ Vector Index ready with " + documentIds.size() + " documents");
    }

    /**
     * Searches for documents most similar to query.
     * @param query query text
     * @param topK number of results to return
     * @return list of SearchResult objects sorted by similarity
     * @throws IOException if embedding generation fails
     */
    public List<SearchResult> search(String query, int topK) throws IOException {
        if (documentEmbeddings.isEmpty()) {
            throw new IllegalStateException("Index not built. Call buildIndex() first.");
        }

        logger.info("Vector search for query: '{}'", query);

        // Embed query
        double[] queryEmbedding = openAIClient.getEmbedding(query, embeddingModel);

        // Compute similarity to all documents
        List<SearchResult> results = new ArrayList<>();
        for (String docId : documentIds) {
            double[] docEmbedding = documentEmbeddings.get(docId);
            double similarity = VectorUtils.cosineSimilarity(queryEmbedding, docEmbedding);

            Document doc = documentStore.get(docId);
            SearchResult result = new SearchResult(doc, similarity, "similarity", null);
            results.add(result);
        }

        // Sort by similarity (highest first)
        results.sort(SearchResult::compareTo);

        // Return top-K
        List<SearchResult> topResults = results.subList(0, Math.min(topK, results.size()));
        logger.info("Vector search returned {} results (top {} requested)", topResults.size(), topK);
        return topResults;
    }

    /**
     * Gets the number of documents indexed.
     * @return number of documents
     */
    public int getDocumentCount() {
        return documentIds.size();
    }

    /**
     * Retrieves embedding vector for document ID.
     * @param docId document ID
     * @return embedding vector or null if not found
     */
    public double[] getEmbedding(String docId) {
        return documentEmbeddings.get(docId);
    }

    /**
     * Clears the index.
     */
    public void clear() {
        documentEmbeddings.clear();
        documentStore.clear();
        documentIds.clear();
        logger.info("Vector Index cleared");
    }
}
