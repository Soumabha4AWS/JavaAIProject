package com.indexing.cli;

import com.indexing.config.ConfigurationManager;
import com.indexing.config.DataLoader;
import com.indexing.index.*;
import com.indexing.model.Document;
import com.indexing.model.SearchResult;
import com.indexing.query.*;
import com.indexing.util.OpenAIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Main CLI application demonstrating all indexing strategies.
 * Mirrors Python demo scripts (vector-index.py, summary-index.py, keyword-index.py, hybrid-index.py).
 * 
 * Usage:
 *   java -Dopenai.api.key=sk-... com.indexing.cli.IndexingDemo
 *   OR
 *   export OPENAI_API_KEY=sk-...
 *   java com.indexing.cli.IndexingDemo
 */
public class IndexingDemo {
    private static final Logger logger = LoggerFactory.getLogger(IndexingDemo.class);

    private static final String TEST_QUERY = "How do I fix authentication issues after password reset?";
    private static final int RESULTS_PER_INDEX = 3;

    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("LlamaIndex Java - Indexing Strategies Demo");
        logger.info("=".repeat(80));

        try {
            // Initialize configuration
            System.out.println("\n" + "=".repeat(80));
            System.out.println("INITIALIZATION");
            System.out.println("=".repeat(80));

            ConfigurationManager config = ConfigurationManager.getInstance();
            System.out.println("\n✓ Configuration loaded");
            System.out.println("  " + config);

            // Load data
            System.out.println("\n" + "=".repeat(80));
            System.out.println("LOADING DATA");
            System.out.println("=".repeat(80));

            List<Document> documents = loadDocuments();
            System.out.println("✓ Loaded " + documents.size() + " documents from synthetic_tickets.json");

            // Initialize OpenAI client
            OpenAIClient openAIClient = new OpenAIClient(config);
            System.out.println("✓ OpenAI client initialized");

            // Build and test indexes
            System.out.println("\n" + "=".repeat(80));
            System.out.println("INDEXING");
            System.out.println("=".repeat(80));

            // Part 1: Vector Index
            demonstrateVectorIndex(openAIClient, config, documents);

            // Part 2: Summary Index
            demonstrateSummaryIndex(openAIClient, config, documents);

            // Part 3: Keyword Index
            demonstrateKeywordIndex(openAIClient, config, documents);

            // Part 4: Hybrid Index
            demonstrateHybridIndex(openAIClient, config, documents);

            System.out.println("\n" + "=".repeat(80));
            System.out.println("✓ ALL DEMONSTRATIONS COMPLETED SUCCESSFULLY");
            System.out.println("=".repeat(80) + "\n");

        } catch (IOException e) {
            logger.error("Error during execution: {}", e.getMessage(), e);
            System.err.println("\n❌ Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            System.err.println("\n❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Demonstrates Vector Index (Part 1).
     */
    private static void demonstrateVectorIndex(OpenAIClient openAIClient, 
                                               ConfigurationManager config,
                                               List<Document> documents) throws IOException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PART 1: Vector Index (Flat Index)");
        System.out.println("=".repeat(80));

        System.out.println("\nVector indexing embeds all chunks and retrieves by semantic similarity.");
        System.out.println("✓ Simple and effective for most use cases");
        System.out.println("✓ Fast similarity search with vector databases");
        System.out.println("✗ No hierarchical structure");
        System.out.println("✗ May return fragmented chunks\n");

        VectorIndex vectorIndex = new VectorIndex(openAIClient, config.getEmbeddingModel());
        vectorIndex.buildIndex(documents);

        VectorQueryEngine vectorQueryEngine = new VectorQueryEngine(vectorIndex);
        QueryResponse response = vectorQueryEngine.query(TEST_QUERY);

        System.out.println("\nTest Query: '" + TEST_QUERY + "'");
        System.out.println("\n" + response.getResponse());

        System.out.println("Source Documents:");
        for (int i = 0; i < Math.min(3, response.getSourceNodes().size()); i++) {
            SearchResult result = response.getSourceNodes().get(i);
            System.out.println(String.format("\n%d. %s", i + 1, 
                result.getDocument().getMetadata().get("ticket_id")));
            System.out.println(String.format("   Score: %.4f", result.getScore()));
            String text = result.getDocument().getText();
            System.out.println("   " + text.substring(0, Math.min(150, text.length())) + "...");
        }
    }

    /**
     * Demonstrates Summary Index (Part 2).
     */
    private static void demonstrateSummaryIndex(OpenAIClient openAIClient,
                                                ConfigurationManager config,
                                                List<Document> documents) throws IOException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PART 2: Summary Index");
        System.out.println("=".repeat(80));

        System.out.println("\nSummary indexing searches through document summaries/titles.");
        System.out.println("✓ Good for high-level queries");
        System.out.println("✓ Returns full documents, not fragments");
        System.out.println("✗ Slower for large datasets (linear scan)");
        System.out.println("✗ No vector similarity search\n");

        System.out.println("⚠️  NOTE: Summary Index makes LLM calls to evaluate relevance.");
        System.out.println("    This may take 1-2 minutes per query. Please wait...\n");

        SummaryIndex summaryIndex = new SummaryIndex(openAIClient, config.getLlmModel());
        summaryIndex.buildIndex(documents);

        SummaryQueryEngine summaryQueryEngine = new SummaryQueryEngine(summaryIndex);
        QueryResponse response = summaryQueryEngine.query(TEST_QUERY);

        System.out.println("\nTest Query: '" + TEST_QUERY + "'");
        System.out.println("\n" + response.getResponse());

        System.out.println("Source Documents:");
        for (int i = 0; i < Math.min(3, response.getSourceNodes().size()); i++) {
            SearchResult result = response.getSourceNodes().get(i);
            System.out.println(String.format("\n%d. %s", i + 1,
                result.getDocument().getMetadata().get("ticket_id")));
            String text = result.getDocument().getText();
            System.out.println("   " + text.substring(0, Math.min(150, text.length())) + "...");
        }
    }

    /**
     * Demonstrates Keyword Index (Part 3).
     */
    private static void demonstrateKeywordIndex(OpenAIClient openAIClient,
                                                ConfigurationManager config,
                                                List<Document> documents) throws IOException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PART 3: Keyword Table Index");
        System.out.println("=".repeat(80));

        System.out.println("\nKeyword indexing extracts keywords and uses exact/fuzzy matching.");
        System.out.println("✓ No embeddings needed - works without vector DB");
        System.out.println("✓ Good for keyword-specific queries");
        System.out.println("✗ No semantic understanding");
        System.out.println("✗ Misses synonyms and related concepts\n");

        System.out.println("⚠️  NOTE: Keyword Index extracts keywords using LLM.");
        System.out.println("    This may take 1-2 minutes. Please wait...\n");

        KeywordIndex keywordIndex = new KeywordIndex(openAIClient, config.getLlmModel());
        keywordIndex.buildIndex(documents);

        System.out.println("✓ Extracted keywords from all documents");
        System.out.println("  Sample keywords: " + 
            keywordIndex.getKeywordTable().keySet().stream()
                .limit(10)
                );

        KeywordQueryEngine keywordQueryEngine = new KeywordQueryEngine(keywordIndex);
        QueryResponse response = keywordQueryEngine.query(TEST_QUERY);

        System.out.println("\nTest Query: '" + TEST_QUERY + "'");
        System.out.println("\n" + response.getResponse());

        System.out.println("Source Documents:");
        for (int i = 0; i < Math.min(3, response.getSourceNodes().size()); i++) {
            SearchResult result = response.getSourceNodes().get(i);
            System.out.println(String.format("\n%d. %s", i + 1,
                result.getDocument().getMetadata().get("ticket_id")));
            String text = result.getDocument().getText();
            System.out.println("   " + text.substring(0, Math.min(150, text.length())) + "...");
        }
    }

    /**
     * Demonstrates Hybrid Index (Part 5).
     */
    private static void demonstrateHybridIndex(OpenAIClient openAIClient,
                                               ConfigurationManager config,
                                               List<Document> documents) throws IOException {
        VectorIndex vectorIndex = new VectorIndex(openAIClient, config.getEmbeddingModel());
        KeywordIndex keywordIndex = new KeywordIndex(openAIClient, config.getLlmModel());
        HybridIndex hybridIndex = new HybridIndex(vectorIndex, keywordIndex);
        hybridIndex.buildIndex(documents);

        HybridQueryEngine hybridQueryEngine = new HybridQueryEngine(hybridIndex);
        QueryResponse response = hybridQueryEngine.query(TEST_QUERY);

        System.out.println("\n" + response.getResponse());

        System.out.println("Source Documents:");
        for (int i = 0; i < Math.min(3, response.getSourceNodes().size()); i++) {
            SearchResult result = response.getSourceNodes().get(i);
            System.out.println(String.format("\n%d. %s", i + 1,
                result.getDocument().getMetadata().get("ticket_id")));
            String text = result.getDocument().getText();
            System.out.println("   " + text.substring(0, Math.min(150, text.length())) + "...");
        }
    }

    /**
     * Loads documents from synthetic_tickets.json.
     * @return list of Document objects
     * @throws IOException if loading fails
     */
    private static List<Document> loadDocuments() throws IOException {
        // Try multiple common locations
        try {
            return DataLoader.loadDocumentsFromDefaultLocation();
        } catch (IOException e1) {
            logger.warn("Could not find synthetic_tickets.json in default locations");
            
            // Try relative to current directory
            String[] paths = {
                "synthetic_tickets.json",
                "../synthetic_tickets.json",
                "../../synthetic_tickets.json"
            };

            for (String path : paths) {
                try {
                    return DataLoader.loadDocuments(path);
                } catch (IOException ignored) {
                    // Try next path
                }
            }

            throw new IOException(
                "Could not find synthetic_tickets.json. " +
                "Please ensure it's in the current directory or a parent directory."
            );
        }
    }
}
