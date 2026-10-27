package com.serverstatus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents server health metrics including CPU and memory usage
 */
public class ServerMetrics {
    @JsonProperty("cpu")
    private String cpu;
    
    @JsonProperty("memory")
    private String memory;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("error")
    private String error;

    public ServerMetrics() {}

    public ServerMetrics(String cpu, String memory, String status) {
        this.cpu = cpu;
        this.memory = memory;
        this.status = status;
    }

    public ServerMetrics(String error) {
        this.error = error;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
