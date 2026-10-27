package com.serverstatus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response model for log queries
 */
public class LogResponse {
    @JsonProperty("logs")
    private List<String> logs;

    public LogResponse() {}

    public LogResponse(List<String> logs) {
        this.logs = logs;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
