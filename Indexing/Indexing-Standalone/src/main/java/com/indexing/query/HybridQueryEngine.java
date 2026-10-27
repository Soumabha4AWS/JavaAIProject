package com.indexing.query;

import com.indexing.index.HybridIndex;
import com.indexing.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Query engine for Hybrid Index.
 * Combines vector and keyword search results using fusion.
 */
public class HybridQueryEngine implements QueryEngine {
    private static final Logger logger = LoggerFactory.getLogger(HybridQueryEngine.class);
    private final HybridIndex hybridIndex;

    public HybridQueryEngine(HybridIndex hybridIndex) {
        this.hybridIndex = hybridIndex;
    }

    @Override
    public QueryResponse query(String queryText) throws IOException {
        logger.info("Hybrid query engine processing: {}", queryText);
        
        List<SearchResult> results = hybridIndex.search(queryText, 5, 5);
        
        StringBuilder response = new StringBuilder();
        response.append("Hybrid Retrieval Results (Combined):\n");
        response.append("Found ").append(results.size()).append(" documents via vector + keyword fusion.\n");
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            response.append(String.format("%d. [Score: %.4f] %s\n", 
                i + 1, 
                result.getScore(),
                result.getDocument().getMetadata().get("ticket_id")));
        }

        return new QueryResponse(response.toString(), results, "hybrid");
    }
}
