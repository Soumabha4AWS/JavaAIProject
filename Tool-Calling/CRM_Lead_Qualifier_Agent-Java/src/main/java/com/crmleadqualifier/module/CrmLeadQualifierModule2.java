package com.crmleadqualifier.module;

import com.crmleadqualifier.CrmLeadQualifierFunctions;
import com.crmleadqualifier.OpenAIConnector;
import com.crmleadqualifier.ToolsSchema;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CrmLeadQualifierModule2 {
    private static final Gson gson = new Gson();

    public static void runAgent(String userPrompt) throws Exception {
        System.out.println("\n--- Running Lead Qualifier Agent (Module 2) ---");

        String systemPrompt = 
            "You are an expert CRM Lead Qualifier Agent. Your sole task is to analyze a sales lead " +
            "provided via email address. You must follow these steps precisely: " +
            "Use your own intelligence for which function to call and in what sequence";

        JsonArray messages = new JsonArray();
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        messages.add(userMsg);

        JsonObject collectedData = new JsonObject();
        JsonArray tools = ToolsSchema.getToolsSchema();

        while (true) {
            System.out.println("\n[AI Thinking...]");

            JsonObject response = OpenAIConnector.callOpenAI("gpt-4o", messages, tools);

            JsonArray choices = response.getAsJsonArray("choices");
            JsonObject choice = choices.get(0).getAsJsonObject();
            JsonObject responseMessage = choice.getAsJsonObject("message");

            JsonObject assistantMsg = new JsonObject();
            assistantMsg.addProperty("role", "assistant");
            assistantMsg.add("content", responseMessage.get("content"));
            if (responseMessage.has("tool_calls")) {
                assistantMsg.add("tool_calls", responseMessage.get("tool_calls"));
            }
            messages.add(assistantMsg);

            JsonElement toolCallsElement = responseMessage.get("tool_calls");
            if (toolCallsElement == null || toolCallsElement.isJsonNull() || toolCallsElement.getAsJsonArray().size() == 0) {
                System.out.println("\n--- FINAL AGENT SUMMARY ---");
                System.out.println(responseMessage.get("content").getAsString());
                break;
            }

            JsonArray toolCalls = toolCallsElement.getAsJsonArray();
            for (int i = 0; i < toolCalls.size(); i++) {
                JsonObject toolCall = toolCalls.get(i).getAsJsonObject();
                String toolCallId = toolCall.get("id").getAsString();
                JsonObject function = toolCall.getAsJsonObject("function");
                String functionName = function.get("name").getAsString();
                String functionArgs = function.get("arguments").getAsString();

                System.out.println("Calling function: " + functionName);
                String functionResult = CrmLeadQualifierFunctions.callFunction(functionName, functionArgs);

                if ("lookup_domain_info".equals(functionName)) {
                    collectedData.add("domain_info", gson.fromJson(functionResult, JsonObject.class));
                } else if ("check_crm_history".equals(functionName)) {
                    collectedData.add("crm_history", gson.fromJson(functionResult, JsonObject.class));
                }

                JsonObject toolResultMsg = new JsonObject();
                toolResultMsg.addProperty("role", "tool");
                toolResultMsg.addProperty("tool_call_id", toolCallId);
                toolResultMsg.addProperty("content", functionResult);
                messages.add(toolResultMsg);
            }
        }
    }
}
