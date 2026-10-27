package com.indexing.query;

import com.indexing.index.KeywordIndex;
import com.indexing.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Query engine for Keyword Index.
 * Returns documents matching query keywords via inverted index.
 */
public class KeywordQueryEngine implements QueryEngine {
    private static final Logger logger = LoggerFactory.getLogger(KeywordQueryEngine.class);
    private final KeywordIndex keywordIndex;

    public KeywordQueryEngine(KeywordIndex keywordIndex) {
        this.keywordIndex = keywordIndex;
    }

    @Override
    public QueryResponse query(String queryText) throws IOException {
        logger.info("Keyword query engine processing: {}", queryText);
        
        List<SearchResult> results = keywordIndex.search(queryText, 3);
        
        StringBuilder response = new StringBuilder();
        response.append("Keyword Index Results:\n");
        response.append("Found ").append(results.size()).append(" documents via keyword matching.\n");
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            response.append(String.format("%d. [Match Score: %.4f] %s\n", 
                i + 1, 
                result.getScore(),
                result.getDocument().getMetadata().get("ticket_id")));
        }

        return new QueryResponse(response.toString(), results, "keyword");
    }
}
