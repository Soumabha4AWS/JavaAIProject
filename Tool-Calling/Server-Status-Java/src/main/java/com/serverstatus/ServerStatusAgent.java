package com.serverstatus;

import com.serverstatus.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Server Status Agent application
 */
public class ServerStatusAgent {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatusAgent.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting Server Status Agent Application");

        // Scenario A: Should trigger a restart (CPU is 98%)
        AgentService.runAgent("The payment-server-01 is extremely slow and timing out.");

        logger.info("\n" + "=".repeat(80) + "\n");

        // Scenario B: Should trigger an escalation (DB is healthy but logs might be weird)
        AgentService.runAgent("Something is wrong with db-node-02.");

        logger.info("\n" + "=".repeat(80) + "\n");

        // Scenario C: The High Memory Case (auth-service-03)
        AgentService.runAgent("Users are reporting login failures on auth-service-03.");

        logger.info("\n" + "=".repeat(80) + "\n");

        // Scenario D: The Dependency Failure (search-index-09)
        AgentService.runAgent("Search isn't working. Can you check search-index-09?");

        logger.info("\n" + "=".repeat(80) + "\n");

        logger.info("Server Status Agent Application Completed");
    }
}
