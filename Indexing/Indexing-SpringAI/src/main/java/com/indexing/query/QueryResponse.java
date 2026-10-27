package com.indexing.query;

import com.indexing.model.SearchResult;
import java.util.List;

/**
 * Response object returned by query engines.
 * Contains synthesized response and source documents.
 */
public class QueryResponse {
    private String response;
    private List<SearchResult> sourceNodes;
    private String indexType;

    public QueryResponse(String response, List<SearchResult> sourceNodes, String indexType) {
        this.response = response;
        this.sourceNodes = sourceNodes;
        this.indexType = indexType;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<SearchResult> getSourceNodes() {
        return sourceNodes;
    }

    public void setSourceNodes(List<SearchResult> sourceNodes) {
        this.sourceNodes = sourceNodes;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    @Override
    public String toString() {
        return "QueryResponse{" +
                "indexType='" + indexType + '\'' +
                ", numSources=" + (sourceNodes != null ? sourceNodes.size() : 0) +
                '}';
    }
}
