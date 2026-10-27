package com.indexing.index;

import com.indexing.model.Document;
import com.indexing.model.SearchResult;
import com.indexing.util.OpenAIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Hybrid Index combining Vector and Keyword indexes.
 * Performs both semantic and exact keyword matching, then fuses results.
 * Provides best of both worlds - semantic understanding + exact matches.
 * Mirrors Python hybrid retrieval patterns from llama_index.
 */
public class HybridIndex {
    private static final Logger logger = LoggerFactory.getLogger(HybridIndex.class);

    private final VectorIndex vectorIndex;
    private final KeywordIndex keywordIndex;
    private boolean isBuilt = false;

    public HybridIndex(VectorIndex vectorIndex, KeywordIndex keywordIndex) {
        this.vectorIndex = vectorIndex;
        this.keywordIndex = keywordIndex;
    }

    /**
     * Builds both vector and keyword indexes.
     * @param documents documents to index
     * @throws IOException if indexing fails
     */
    public void buildIndex(List<Document> documents) throws IOException {
        logger.info("Building Hybrid Index with {} documents...", documents.size());
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PART 5: Hybrid Retrieval");
        System.out.println("=".repeat(80));
        System.out.println("\nHybrid retrieval combines vector (semantic) + keyword (exact) search.");
        System.out.println("✓ Best of both worlds - semantic + exact matching");
        System.out.println("✓ More robust to query variations");
        System.out.println("✗ Slower (multiple searches)");
        System.out.println("✗ Requires result fusion logic\n");
        System.out.println("✓ Using Vector + Keyword hybrid approach\n");

        vectorIndex.buildIndex(documents);
        keywordIndex.buildIndex(documents);
        
        isBuilt = true;
        logger.info("✓ Hybrid Index built successfully");
        System.out.println("✓ Hybrid Index ready");
    }

    /**
     * Searches using hybrid approach.
     * Performs vector search and keyword search, then fuses results.
     * @param query query text
     * @param vectorTopK number of vector results to retrieve
     * @param keywordTopK number of keyword results to retrieve
     * @return list of fused SearchResult objects
     * @throws IOException if search fails
     */
    public List<SearchResult> search(String query, int vectorTopK, int keywordTopK) throws IOException {
        if (!isBuilt) {
            throw new IllegalStateException("Index not built. Call buildIndex() first.");
        }

        logger.info("Hybrid search for query: '{}'", query);
        System.out.println("Test Query: '" + query + "'");

        // Step 1: Vector search (semantic)
        System.out.println("\n--- Step 1: Vector Search (Semantic) ---");
        List<SearchResult> vectorResults = vectorIndex.search(query, vectorTopK);
        logger.info("Vector search returned {} results", vectorResults.size());

        // Step 2: Keyword search (exact)
        System.out.println("--- Step 2: Keyword Search (Exact) ---");
        List<SearchResult> keywordResults = keywordIndex.search(query, keywordTopK);
        logger.info("Keyword search returned {} results", keywordResults.size());

        // Step 3: Fusion - Simple union with deduplication
        System.out.println("--- Step 3: Fusion (Union) ---");
        List<SearchResult> fusedResults = fuseResults(vectorResults, keywordResults);
        
        logger.info("Hybrid search returned {} fused results", fusedResults.size());
        return fusedResults;
    }

    /**
     * Fuses vector and keyword search results using simple union strategy.
     * Results found by both methods are prioritized.
     * @param vectorResults results from vector search
     * @param keywordResults results from keyword search
     * @return fused results list
     */
    private List<SearchResult> fuseResults(List<SearchResult> vectorResults, 
                                           List<SearchResult> keywordResults) {
        Map<String, SearchResult> fusedMap = new LinkedHashMap<>();
        Set<String> foundByBoth = new HashSet<>();

        // Track which documents appear in both result sets
        Set<String> vectorDocIds = new HashSet<>();
        for (SearchResult result : vectorResults) {
            vectorDocIds.add(result.getDocument().getNodeId());
        }

        Set<String> keywordDocIds = new HashSet<>();
        for (SearchResult result : keywordResults) {
            String docId = result.getDocument().getNodeId();
            keywordDocIds.add(docId);
            if (vectorDocIds.contains(docId)) {
                foundByBoth.add(docId);
            }
        }

        // Add vector results first (prioritize semantic matches)
        for (SearchResult result : vectorResults) {
            fusedMap.put(result.getDocument().getNodeId(), result);
        }

        // Add keyword results (but don't overwrite vector matches)
        for (SearchResult result : keywordResults) {
            String docId = result.getDocument().getNodeId();
            if (!fusedMap.containsKey(docId)) {
                fusedMap.put(docId, result);
            }
        }

        // Sort: results found by both first, then others by score
        List<SearchResult> fused = new ArrayList<>(fusedMap.values());
        fused.sort((a, b) -> {
            String aId = a.getDocument().getNodeId();
            String bId = b.getDocument().getNodeId();
            
            // Both methods found it - highest priority
            boolean aInBoth = foundByBoth.contains(aId);
            boolean bInBoth = foundByBoth.contains(bId);
            
            if (aInBoth && !bInBoth) return -1;
            if (!aInBoth && bInBoth) return 1;
            
            // Otherwise sort by score
            return Double.compare(b.getScore(), a.getScore());
        });

        return fused;
    }

    /**
     * Gets the vector index.
     * @return vector index
     */
    public VectorIndex getVectorIndex() {
        return vectorIndex;
    }

    /**
     * Gets the keyword index.
     * @return keyword index
     */
    public KeywordIndex getKeywordIndex() {
        return keywordIndex;
    }

    /**
     * Clears both indexes.
     */
    public void clear() {
        vectorIndex.clear();
        keywordIndex.clear();
        isBuilt = false;
        logger.info("Hybrid Index cleared");
    }
}
