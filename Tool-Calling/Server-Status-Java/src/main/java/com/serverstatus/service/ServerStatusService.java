package com.serverstatus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverstatus.model.LogResponse;
import com.serverstatus.model.ServerMetrics;
import com.serverstatus.model.ToolResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for server status operations and tool functions
 */
public class ServerStatusService {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatusService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, ServerMetrics> METRICS = new HashMap<>();
    private static final Map<String, List<String>> LOG_DATABASE = new HashMap<>();

    static {
        // Initialize metrics data
        METRICS.put("payment-server-01", new ServerMetrics("98%", "40%", "Warning"));
        METRICS.put("db-node-02", new ServerMetrics("12%", "60%", "Healthy"));
        METRICS.put("auth-service-03", new ServerMetrics("45%", "95%", "Critical"));
        METRICS.put("search-index-09", new ServerMetrics("10%", "15%", "Error"));
        METRICS.put("frontend-node-04", new ServerMetrics("25%", "30%", "Healthy"));

        // Initialize log database
        LOG_DATABASE.put("payment-server-01", List.of(
                "[INFO] Request received /pay/v1",
                "[WARN] CPU threshold exceeded 90%",
                "[WARN] Thread pool exhaustion",
                "[CRITICAL] Process hung, not accepting new connections",
                "[ERROR] Timeout waiting for thread"
        ));

        LOG_DATABASE.put("db-node-02", List.of(
                "[INFO] Backup started",
                "[INFO] Backup completed successfully",
                "[INFO] User query executed in 12ms",
                "[INFO] Health check: OK",
                "[INFO] Replication sync active"
        ));

        LOG_DATABASE.put("auth-service-03", List.of(
                "[INFO] Token validated user_882",
                "[WARN] Garbage collection taking too long (>5s)",
                "[ERROR] java.lang.OutOfMemoryError: Java heap space",
                "[CRITICAL] Application crashing due to memory leak",
                "[INFO] Restarting context..."
        ));

        LOG_DATABASE.put("search-index-09", List.of(
                "[INFO] Indexing started",
                "[ERROR] Connection refused: elastic-cluster-main:9200",
                "[ERROR] Failed to write document ID 4432",
                "[CRITICAL] Dependency Unreachable: Search Engine is down",
                "[ERROR] Retrying in 30s..."
        ));

        LOG_DATABASE.put("frontend-node-04", List.of(
                "[INFO] GET /home 200 OK",
                "[INFO] GET /assets/logo.png 200 OK",
                "[INFO] GET /login 200 OK",
                "[INFO] GET /api/v1/status 200 OK",
                "[INFO] Health check passed"
        ));
    }

    /**
     * Checks server health metrics
     */
    public String getServerHealth(String serverId) {
        logger.info("-> TOOL: Checking health for {}...", serverId);
        
        try {
            ServerMetrics metrics = METRICS.getOrDefault(serverId, new ServerMetrics("Server not found. Check the ID."));
            return objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing server health", e);
        }
    }

    /**
     * Fetches recent logs for a server
     */
    public String fetchRecentLogs(String serverId, int lines) {
        logger.info("-> TOOL: Fetching last {} log lines for {}...", lines, serverId);
        
        try {
            List<String> defaultLogs = List.of(
                    "[INFO] System stable",
                    "[INFO] Heartbeat signal received"
            );

            List<String> serverLogs = LOG_DATABASE.getOrDefault(serverId, defaultLogs);
            int endIndex = Math.min(lines, serverLogs.size());
            LogResponse response = new LogResponse(serverLogs.subList(0, endIndex));
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing logs", e);
        }
    }

    /**
     * Restarts a service
     */
    public String restartService(String serverId) {
        logger.info("\nTOOL: Restarting service...");
        
        try {
            ToolResponse response = new ToolResponse("success", "Server restarted successfully");
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing restart response", e);
        }
    }

    /**
     * Escalates issue to engineer
     */
    public String escalateToEngineer(String summary) {
        logger.info("\nTOOL: Escalating to human...");
        
        try {
            ToolResponse response = new ToolResponse("success", "The ticket was created successfully");
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing escalation response", e);
        }
    }
}
