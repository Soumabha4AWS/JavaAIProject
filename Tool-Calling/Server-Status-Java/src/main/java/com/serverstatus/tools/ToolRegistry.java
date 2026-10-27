package com.serverstatus.tools;

import com.serverstatus.service.ServerStatusService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry for available tool operations
 */
public class ToolRegistry {
    private static final ServerStatusService serverStatusService = new ServerStatusService();
    private static final Map<String, Function<Map<String, Object>, String>> AVAILABLE_FUNCTIONS = new HashMap<>();

    static {
        AVAILABLE_FUNCTIONS.put("get_server_health", args -> {
            String serverId = (String) args.get("server_id");
            return serverStatusService.getServerHealth(serverId);
        });

        AVAILABLE_FUNCTIONS.put("fetch_recent_logs", args -> {
            String serverId = (String) args.get("server_id");
            int lines = args.containsKey("lines") ? ((Number) args.get("lines")).intValue() : 5;
            return serverStatusService.fetchRecentLogs(serverId, lines);
        });

        AVAILABLE_FUNCTIONS.put("restart_service", args -> {
            String serverId = (String) args.get("server_id");
            return serverStatusService.restartService(serverId);
        });

        AVAILABLE_FUNCTIONS.put("escalate_to_engineer", args -> {
            String summary = (String) args.get("summary");
            return serverStatusService.escalateToEngineer(summary);
        });
    }

    /**
     * Executes a tool function by name with given arguments
     * @param functionName The name of the function to execute
     * @param arguments The arguments for the function
     * @return The result of the function execution
     */
    public static String executeFunction(String functionName, Map<String, Object> arguments) {
        Function<Map<String, Object>, String> function = AVAILABLE_FUNCTIONS.get(functionName);
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }
        return function.apply(arguments);
    }

    /**
     * Checks if a function is available
     */
    public static boolean hasFunction(String functionName) {
        return AVAILABLE_FUNCTIONS.containsKey(functionName);
    }
}
