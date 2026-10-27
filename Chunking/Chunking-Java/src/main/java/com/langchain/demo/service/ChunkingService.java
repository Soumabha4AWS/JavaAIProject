package com.langchain.demo.service;

import dev.langchain4j.data.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for demonstrating various text chunking strategies
 */
public class ChunkingService {
    private static final Logger logger = LoggerFactory.getLogger(ChunkingService.class);

    /**
     * Strategy 1: Fixed-Size Chunking
     * Splits text into equal-sized chunks based on character count with overlap
     *
     * @param documents List of documents to chunk
     * @param chunkSize Maximum characters per chunk
     * @param overlap Characters to repeat between chunks
     * @return List of chunked documents
     */
    public List<Document> fixedSizeChunking(List<Document> documents, int chunkSize, int overlap) {
        logger.info("Applying fixed-size chunking: chunkSize={}, overlap={}", chunkSize, overlap);

        List<Document> chunks = new ArrayList<>();
        
        for (Document doc : documents) {
            String text = doc.text();
            for (int i = 0; i < text.length(); i += (chunkSize - overlap)) {
                int end = Math.min(i + chunkSize, text.length());
                String chunkText = text.substring(i, end);
                Document chunk = new Document(chunkText, doc.metadata());
                chunks.add(chunk);
            }
        }
        
        return chunks;
    }

    /**
     * Strategy 2: Recursive Character Splitting (RECOMMENDED)
     * Tries to split on natural boundaries (paragraphs → sentences → words → characters)
     *
     * @param documents List of documents to chunk
     * @param chunkSize Maximum characters per chunk
     * @param overlap Characters to repeat between chunks
     * @return List of chunked documents
     */
    public List<Document> recursiveChunking(List<Document> documents, int chunkSize, int overlap) {
        logger.info("Applying recursive character splitting: chunkSize={}, overlap={}", chunkSize, overlap);

        List<Document> chunks = new ArrayList<>();
        String[] separators = {"\n\n", "\n", ". ", " "};
        
        for (Document doc : documents) {
            chunks.addAll(recursiveSplit(doc, chunkSize, overlap, separators, 0));
        }
        
        return chunks;
    }

    /**
     * Recursive helper method for splitting on natural boundaries
     */
    private List<Document> recursiveSplit(Document doc, int chunkSize, int overlap, 
                                         String[] separators, int separatorIndex) {
        List<Document> chunks = new ArrayList<>();
        String text = doc.text();
        
        if (text.length() <= chunkSize) {
            chunks.add(doc);
            return chunks;
        }
        
        if (separatorIndex >= separators.length) {
            // Last resort: split by character
            for (int i = 0; i < text.length(); i += (chunkSize - overlap)) {
                int end = Math.min(i + chunkSize, text.length());
                String chunkText = text.substring(i, end);
                Document chunk = new Document(chunkText, doc.metadata());
                chunks.add(chunk);
            }
            return chunks;
        }
        
        String separator = separators[separatorIndex];
        String[] parts = text.split("\\" + separator.replaceAll("\\\\", "\\\\\\\\"));
        
        List<String> goodChunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String testChunk = currentChunk.length() == 0 
                ? part 
                : currentChunk + separator + part;
            
            if (testChunk.length() <= chunkSize) {
                currentChunk = new StringBuilder(testChunk);
            } else {
                if (currentChunk.length() > 0) {
                    goodChunks.add(currentChunk.toString());
                }
                currentChunk = new StringBuilder(part);
            }
        }
        
        if (currentChunk.length() > 0) {
            goodChunks.add(currentChunk.toString());
        }
        
        // If chunks are still too large, recursively split with next separator
        for (String chunk : goodChunks) {
            if (chunk.length() > chunkSize) {
                Document tempDoc = new Document(chunk, doc.metadata());
                chunks.addAll(recursiveSplit(tempDoc, chunkSize, overlap, separators, separatorIndex + 1));
            } else {
                Document chunkDoc = new Document(chunk, doc.metadata());
                chunks.add(chunkDoc);
            }
        }
        
        return chunks;
    }

    /**
     * Strategy 3: No Chunking (Whole Documents)
     * Returns documents as-is without splitting
     * Best for documents that are already small and self-contained
     *
     * @param documents List of documents to use as chunks
     * @return The documents unchanged
     */
    public List<Document> wholeDocumentChunking(List<Document> documents) {
        logger.info("Using whole documents without chunking (total: {})", documents.size());
        return new ArrayList<>(documents);
    }

    /**
     * Analyze and report chunking statistics
     *
     * @param originalDocs Original documents before chunking
     * @param chunkedDocs Chunked documents
     * @param strategy Name of the chunking strategy
     */
    public void reportChunkingStats(List<Document> originalDocs, List<Document> chunkedDocs, String strategy) {
        long totalChars = chunkedDocs.stream()
                .mapToLong(doc -> doc.text().length())
                .sum();

        double avgChunkSize = totalChars / (double) chunkedDocs.size();

        logger.info("=== Chunking Statistics for {} ===", strategy);
        logger.info("Original documents: {}", originalDocs.size());
        logger.info("Total chunks: {}", chunkedDocs.size());
        logger.info("Average chunk size: {:.0f} characters", avgChunkSize);
        logger.info("Total characters: {}", totalChars);

        if (chunkedDocs.size() > 0) {
            long minSize = chunkedDocs.stream()
                    .mapToLong(doc -> doc.text().length())
                    .min()
                    .orElse(0);
            long maxSize = chunkedDocs.stream()
                    .mapToLong(doc -> doc.text().length())
                    .max()
                    .orElse(0);
            logger.info("Min chunk size: {}, Max chunk size: {}", minSize, maxSize);
        }
    }
}
