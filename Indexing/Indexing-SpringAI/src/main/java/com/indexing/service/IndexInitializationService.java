package com.indexing.service;

import com.indexing.index.HybridIndex;
import com.indexing.index.KeywordIndex;
import com.indexing.index.SummaryIndex;
import com.indexing.index.VectorIndex;
import com.indexing.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Builds all indexes asynchronously after the Spring context is fully started.
 * This ensures the application starts up and serves HTTP requests immediately,
 * while indexes are built in the background.
 */
@Service
public class IndexInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(IndexInitializationService.class);

    private final VectorIndex vectorIndex;
    private final SummaryIndex summaryIndex;
    private final KeywordIndex keywordIndex;
    private final HybridIndex hybridIndex;
    private final List<Document> documents;

    private final AtomicBoolean vectorReady   = new AtomicBoolean(false);
    private final AtomicBoolean summaryReady  = new AtomicBoolean(false);
    private final AtomicBoolean keywordReady  = new AtomicBoolean(false);
    private final AtomicBoolean hybridReady   = new AtomicBoolean(false);

    public IndexInitializationService(VectorIndex vectorIndex,
                                      SummaryIndex summaryIndex,
                                      KeywordIndex keywordIndex,
                                      HybridIndex hybridIndex,
                                      List<Document> documents) {
        this.vectorIndex  = vectorIndex;
        this.summaryIndex = summaryIndex;
        this.keywordIndex = keywordIndex;
        this.hybridIndex  = hybridIndex;
        this.documents    = documents;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void buildAllIndexes() {
        logger.info("=== Starting background index initialization for {} documents ===", documents.size());

        buildVector();
        buildSummary();
        buildKeyword();
        buildHybrid();

        logger.info("=== All indexes initialized successfully ===");
    }

    private void buildVector() {
        try {
            logger.info("Building Vector Index...");
            vectorIndex.buildIndex(documents);
            vectorReady.set(true);
            logger.info("✓ Vector Index ready");
        } catch (Exception e) {
            logger.error("Failed to build Vector Index: {}", e.getMessage(), e);
        }
    }

    private void buildSummary() {
        try {
            logger.info("Building Summary Index...");
            summaryIndex.buildIndex(documents);
            summaryReady.set(true);
            logger.info("✓ Summary Index ready");
        } catch (Exception e) {
            logger.error("Failed to build Summary Index: {}", e.getMessage(), e);
        }
    }

    private void buildKeyword() {
        try {
            logger.info("Building Keyword Index...");
            keywordIndex.buildIndex(documents);
            keywordReady.set(true);
            logger.info("✓ Keyword Index ready");
        } catch (Exception e) {
            logger.error("Failed to build Keyword Index: {}", e.getMessage(), e);
        }
    }

    private void buildHybrid() {
        try {
            logger.info("Building Hybrid Index...");
            hybridIndex.buildIndex(documents);
            hybridReady.set(true);
            logger.info("✓ Hybrid Index ready");
        } catch (Exception e) {
            logger.error("Failed to build Hybrid Index: {}", e.getMessage(), e);
        }
    }

    public boolean isVectorReady()   { return vectorReady.get(); }
    public boolean isSummaryReady()  { return summaryReady.get(); }
    public boolean isKeywordReady()  { return keywordReady.get(); }
    public boolean isHybridReady()   { return hybridReady.get(); }
}
