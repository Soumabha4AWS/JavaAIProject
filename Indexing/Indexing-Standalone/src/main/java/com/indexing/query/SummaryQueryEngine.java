package com.indexing.query;

import com.indexing.index.SummaryIndex;
import com.indexing.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Query engine for Summary Index.
 * Returns documents evaluated as relevant by LLM.
 */
public class SummaryQueryEngine implements QueryEngine {
    private static final Logger logger = LoggerFactory.getLogger(SummaryQueryEngine.class);
    private final SummaryIndex summaryIndex;

    public SummaryQueryEngine(SummaryIndex summaryIndex) {
        this.summaryIndex = summaryIndex;
    }

    @Override
    public QueryResponse query(String queryText) throws IOException {
        logger.info("Summary query engine processing: {}", queryText);
        
        List<SearchResult> results = summaryIndex.search(queryText, 3);
        
        StringBuilder response = new StringBuilder();
        response.append("Summary Index Results:\n");
        response.append("Found ").append(results.size()).append(" relevant documents via LLM evaluation.\n");
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            response.append(String.format("%d. [Relevance: %.4f] %s\n", 
                i + 1, 
                result.getScore(),
                result.getDocument().getMetadata().get("ticket_id")));
        }

        return new QueryResponse(response.toString(), results, "summary");
    }
}
