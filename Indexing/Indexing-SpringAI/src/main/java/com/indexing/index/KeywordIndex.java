package com.indexing.index;

import com.indexing.model.Document;
import com.indexing.model.SearchResult;
import com.indexing.util.OpenAIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Keyword Table Index implementation using inverted index.
 * Extracts keywords from documents and builds keyword→document mappings.
 * Mirrors Python KeywordTableIndex from llama_index.
 */
public class KeywordIndex {
    private static final Logger logger = LoggerFactory.getLogger(KeywordIndex.class);

    private final OpenAIClient openAIClient;
    private final String llmModel;
    private final Map<String, Set<String>> keywordToDocIds; // keyword → {doc_ids}
    private final Map<String, Document> documentStore;
    private final Map<String, List<String>> docIdToKeywords; // doc_id → [keywords]

    public KeywordIndex(OpenAIClient openAIClient, String llmModel) {
        this.openAIClient = openAIClient;
        this.llmModel = llmModel;
        this.keywordToDocIds = new HashMap<>();
        this.documentStore = new HashMap<>();
        this.docIdToKeywords = new HashMap<>();
    }

    /**
     * Builds keyword index from documents.
     * Extracts keywords and builds inverted index.
     * @param documents documents to index
     * @throws IOException if keyword extraction fails
     */
    public void buildIndex(List<Document> documents) throws IOException {
        logger.info("Building Keyword Index with {} documents...", documents.size());
        System.out.println("\nBuilding Keyword Index from documents...");

        int processed = 0;
        for (Document doc : documents) {
            try {
                String docId = doc.getNodeId();
                documentStore.put(docId, doc);

                // Extract keywords
                List<String> keywords = extractKeywords(doc.getText());
                docIdToKeywords.put(docId, keywords);

                // Add to inverted index
                for (String keyword : keywords) {
                    keywordToDocIds.computeIfAbsent(keyword, k -> new HashSet<>()).add(docId);
                }

                processed++;
                if (processed % 5 == 0) {
                    System.out.println("  ✓ Processed " + processed + " documents...");
                }
            } catch (IOException e) {
                logger.error("Failed to extract keywords from document {}: {}", doc.getNodeId(), e.getMessage());
                throw e;
            }
        }

        logger.info("✓ Keyword Index built with {} unique keywords", keywordToDocIds.size());
        System.out.println("✓ Keyword Index ready with " + keywordToDocIds.size() + " unique keywords");
        System.out.println("  Keywords sample: " + 
            keywordToDocIds.keySet().stream()
                .limit(10)
                .collect(Collectors.joining(", ")));
    }

    /**
     * Searches using keyword matching.
     * @param query query text
     * @param topK number of results to return
     * @return list of SearchResult objects sorted by keyword match count
     * @throws IOException if keyword extraction fails
     */
    public List<SearchResult> search(String query, int topK) throws IOException {
        if (keywordToDocIds.isEmpty()) {
            throw new IllegalStateException("Index not built. Call buildIndex() first.");
        }

        logger.info("Keyword search for query: '{}'", query);

        // Extract keywords from query
        List<String> queryKeywords = extractKeywords(query);
        logger.info("Query keywords: {}", queryKeywords);

        // Count keyword matches per document
        Map<String, Integer> docKeywordCount = new HashMap<>();
        for (String keyword : queryKeywords) {
            Set<String> matchingDocs = keywordToDocIds.get(keyword);
            if (matchingDocs != null) {
                for (String docId : matchingDocs) {
                    docKeywordCount.put(docId, docKeywordCount.getOrDefault(docId, 0) + 1);
                }
            }
        }

        // Create SearchResults sorted by match count
        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : docKeywordCount.entrySet()) {
            String docId = entry.getKey();
            int matchCount = entry.getValue();
            Document doc = documentStore.get(docId);

            if (doc != null) {
                double score = (double) matchCount / queryKeywords.size();
                SearchResult result = new SearchResult(doc, score, "keyword_count", 
                    Map.of("matches", matchCount, "query_keywords", queryKeywords.size()));
                results.add(result);
            }
        }

        // Sort by score (highest first)
        results.sort(SearchResult::compareTo);

        // Return top-K
        List<SearchResult> topResults = results.subList(0, Math.min(topK, results.size()));
        logger.info("Keyword search returned {} results (top {} requested)", topResults.size(), topK);
        return topResults;
    }

    /**
     * Extracts keywords from text.
     * Uses LLM for accurate keyword extraction.
     * @param text text to extract keywords from
     * @return list of keywords
     * @throws IOException if LLM call fails
     */
    private List<String> extractKeywords(String text) throws IOException {
        try {
            return openAIClient.extractKeywords(text, llmModel);
        } catch (IOException e) {
            logger.warn("LLM keyword extraction failed, falling back to simple regex");
            return simpleKeywordExtraction(text);
        }
    }

    /**
     * Simple keyword extraction using regex (fallback).
     * Extracts capitalized words and common terms.
     * @param text text to extract from
     * @return list of keywords
     */
    private List<String> simpleKeywordExtraction(String text) {
        Set<String> keywords = new HashSet<>();
        
        // Extract words with capital letters (likely important)
        Pattern capitalPattern = Pattern.compile("\\b[A-Z][a-z]+\\b");
        var matcher = capitalPattern.matcher(text);
        while (matcher.find()) {
            keywords.add(matcher.group().toLowerCase());
        }

        // Extract common technical terms
        String[] terms = text.toLowerCase().split("\\W+");
        for (String term : terms) {
            if (term.length() > 4 && !isCommonWord(term)) {
                keywords.add(term);
            }
        }

        return new ArrayList<>(keywords);
    }

    /**
     * Checks if word is a common English word (stopword).
     * @param word word to check
     * @return true if word is common
     */
    private boolean isCommonWord(String word) {
        Set<String> commonWords = Set.of(
            "the", "is", "at", "which", "on", "a", "an", "and", "or", "but", 
            "in", "with", "to", "for", "of", "be", "have", "has", "had", "do"
        );
        return commonWords.contains(word);
    }

    /**
     * Gets the keyword table (inverted index).
     * @return map of keyword to document IDs
     */
    public Map<String, Set<String>> getKeywordTable() {
        return new HashMap<>(keywordToDocIds);
    }

    /**
     * Gets keywords for a document.
     * @param docId document ID
     * @return list of keywords for document
     */
    public List<String> getDocumentKeywords(String docId) {
        return docIdToKeywords.getOrDefault(docId, new ArrayList<>());
    }

    /**
     * Gets the number of documents indexed.
     * @return number of documents
     */
    public int getDocumentCount() {
        return documentStore.size();
    }

    /**
     * Clears the index.
     */
    public void clear() {
        keywordToDocIds.clear();
        documentStore.clear();
        docIdToKeywords.clear();
        logger.info("Keyword Index cleared");
    }
}
