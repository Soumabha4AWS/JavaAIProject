package com.serverstatus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic response model for tool operations
 */
public class ToolResponse {
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;

    public ToolResponse() {}

    public ToolResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
