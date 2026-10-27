package com.indexing.query;

import com.indexing.model.SearchResult;
import java.io.IOException;
import java.util.List;

/**
 * Base interface for query engines.
 * All query engine implementations must provide a query() method.
 */
public interface QueryEngine {
    /**
     * Executes a query and returns results.
     * @param query query text
     * @return query response with results and source documents
     * @throws IOException if query execution fails
     */
    QueryResponse query(String query) throws IOException;
}
