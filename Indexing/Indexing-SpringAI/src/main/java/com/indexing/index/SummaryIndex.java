package com.indexing.index;

import com.indexing.model.Document;
import com.indexing.model.SearchResult;
import com.indexing.util.OpenAIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Summary Index implementation using LLM-based relevance evaluation.
 * Evaluates each document for relevance to query without embeddings.
 * Slower but good for high-level queries that need full document context.
 * Mirrors Python SummaryIndex from llama_index.
 */
public class SummaryIndex {
    private static final Logger logger = LoggerFactory.getLogger(SummaryIndex.class);

    private final OpenAIClient openAIClient;
    private final String llmModel;
    private final Map<String, Document> documentStore;
    private final List<String> documentIds;

    public SummaryIndex(OpenAIClient openAIClient, String llmModel) {
        this.openAIClient = openAIClient;
        this.llmModel = llmModel;
        this.documentStore = new HashMap<>();
        this.documentIds = new ArrayList<>();
    }

    /**
     * Builds index from documents (minimal work - just store documents).
     * No embeddings needed.
     * @param documents documents to index
     */
    public void buildIndex(List<Document> documents) {
        logger.info("Building Summary Index with {} documents...", documents.size());
        System.out.println("\nBuilding Summary Index from documents...");

        for (Document doc : documents) {
            String docId = doc.getNodeId();
            documentStore.put(docId, doc);
            documentIds.add(docId);
        }

        logger.info("✓ Summary Index built successfully with {} documents", documentIds.size());
        System.out.println("✓ Summary Index ready with " + documentIds.size() + " documents");
    }

    /**
     * Searches by evaluating each document for relevance to query.
     * Uses LLM to determine relevance.
     * @param query query text
     * @param topK number of results to return
     * @return list of SearchResult objects sorted by relevance
     * @throws IOException if LLM calls fail
     */
    public List<SearchResult> search(String query, int topK) throws IOException {
        if (documentStore.isEmpty()) {
            throw new IllegalStateException("Index not built. Call buildIndex() first.");
        }

        logger.info("Summary index search for query: '{}'", query);
        System.out.println("\n  Evaluating " + documentIds.size() + " documents for relevance (this may take a moment)...");

        List<SearchResult> results = new ArrayList<>();

        // Parallel evaluation with limited thread pool
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<SearchResult>> futures = new ArrayList<>();

        for (String docId : documentIds) {
            Document doc = documentStore.get(docId);
            futures.add(executor.submit(() -> evaluateRelevance(query, doc)));
        }

        // Collect results
        for (Future<SearchResult> future : futures) {
            try {
                SearchResult result = future.get(60, TimeUnit.SECONDS);
                if (result != null) {
                    results.add(result);
                }
            } catch (TimeoutException e) {
                logger.warn("Relevance evaluation timed out for document");
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error during relevance evaluation: {}", e.getMessage());
            }
        }

        executor.shutdown();

        // Sort by relevance score (highest first)
        results.sort(SearchResult::compareTo);

        // Return top-K
        List<SearchResult> topResults = results.subList(0, Math.min(topK, results.size()));
        logger.info("Summary search returned {} results (top {} requested)", topResults.size(), topK);
        return topResults;
    }

    /**
     * Evaluates if a document is relevant to the query.
     * Uses LLM to make relevance judgment.
     * @param query query text
     * @param doc document to evaluate
     * @return SearchResult with relevance score
     */
    private SearchResult evaluateRelevance(String query, Document doc) {
        try {
            // Create prompt for LLM relevance evaluation
            String prompt = String.format(
                "Given the following question and document, determine if the document is relevant to answering the question.\n" +
                "Rate relevance on a scale of 0-1, where 1 is highly relevant and 0 is not relevant at all.\n" +
                "Return ONLY a number between 0 and 1.\n\n" +
                "Question: %s\n\n" +
                "Document:\n%s",
                query,
                doc.getText().substring(0, Math.min(500, doc.getText().length()))
            );

            String response = openAIClient.callLLM(prompt, llmModel);
            double relevance = parseRelevanceScore(response);

            return new SearchResult(doc, relevance, "relevance", null);
        } catch (IOException e) {
            logger.warn("Failed to evaluate relevance for document {}: {}", 
                       doc.getNodeId(), e.getMessage());
            return new SearchResult(doc, 0.0, "relevance", null);
        }
    }

    /**
     * Parses relevance score from LLM response.
     * @param response LLM response text
     * @return relevance score (0-1)
     */
    private double parseRelevanceScore(String response) {
        try {
            // Extract number from response
            String clean = response.trim()
                .replaceAll("[^0-9.]", "")
                .substring(0, Math.min(4, response.length()));
            
            double score = Double.parseDouble(clean);
            return Math.max(0.0, Math.min(1.0, score)); // Clamp to 0-1
        } catch (Exception e) {
            logger.warn("Could not parse relevance score from response: {}", response);
            return 0.0;
        }
    }

    /**
     * Gets the number of documents indexed.
     * @return number of documents
     */
    public int getDocumentCount() {
        return documentIds.size();
    }

    /**
     * Clears the index.
     */
    public void clear() {
        documentStore.clear();
        documentIds.clear();
        logger.info("Summary Index cleared");
    }
}
