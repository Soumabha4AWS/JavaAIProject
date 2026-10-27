package com.indexing.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document with text content and metadata.
 * Mirrors Python LlamaIndex Document class.
 */
public class Document {
    private String text;
    private Map<String, Object> metadata;
    private String nodeId;

    public Document(String text) {
        this(text, new HashMap<>());
    }

    public Document(String text, Map<String, Object> metadata) {
        this.text = text;
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.nodeId = System.nanoTime() + "-" + hashCode();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "Document{" +
                "nodeId='" + nodeId + '\'' +
                ", text='" + text.substring(0, Math.min(100, text.length())) + "...'" +
                ", metadata=" + metadata +
                '}';
    }
}
