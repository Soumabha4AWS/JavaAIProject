package com.indexing.query;

import com.indexing.index.VectorIndex;
import com.indexing.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Query engine for Vector Index.
 * Returns semantically similar documents.
 */
public class VectorQueryEngine implements QueryEngine {
    private static final Logger logger = LoggerFactory.getLogger(VectorQueryEngine.class);
    private final VectorIndex vectorIndex;

    public VectorQueryEngine(VectorIndex vectorIndex) {
        this.vectorIndex = vectorIndex;
    }

    @Override
    public QueryResponse query(String queryText) throws IOException {
        logger.info("Vector query engine processing: {}", queryText);
        
        List<SearchResult> results = vectorIndex.search(queryText, 3);
        
        StringBuilder response = new StringBuilder();
        response.append("Vector Index Results:\n");
        response.append("Found ").append(results.size()).append(" semantically similar documents.\n");
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            response.append(String.format("%d. [Score: %.4f] %s\n", 
                i + 1, 
                result.getScore(),
                result.getDocument().getMetadata().get("ticket_id")));
        }

        return new QueryResponse(response.toString(), results, "vector");
    }
}
