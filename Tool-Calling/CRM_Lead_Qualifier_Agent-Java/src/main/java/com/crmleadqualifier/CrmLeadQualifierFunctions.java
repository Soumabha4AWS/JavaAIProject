package com.crmleadqualifier;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class CrmLeadQualifierFunctions {
    private static final Gson gson = new Gson();

    private static final Map<String, Map<String, String>> mockDomainData = new HashMap<>();
    private static final Map<String, Map<String, String>> mockCrmData = new HashMap<>();

    static {
        // Initialize mock domain data
        Map<String, String> acmeCorp = new HashMap<>();
        acmeCorp.put("industry", "Software/SaaS");
        acmeCorp.put("size", "501-1000 employees");
        acmeCorp.put("revenue", "$50M - $100M");
        mockDomainData.put("acmecorp.com", acmeCorp);

        Map<String, String> widgetCo = new HashMap<>();
        widgetCo.put("industry", "Manufacturing");
        widgetCo.put("size", "100-250 employees");
        widgetCo.put("revenue", "$10M - $25M");
        mockDomainData.put("widgetco.net", widgetCo);

        Map<String, String> globalFin = new HashMap<>();
        globalFin.put("industry", "Financial Services");
        globalFin.put("size", "5000+ employees");
        globalFin.put("revenue", "$1B+");
        mockDomainData.put("globalfin.org", globalFin);

        // Initialize mock CRM data
        Map<String, String> jane = new HashMap<>();
        jane.put("last_contact", "2025-11-15");
        jane.put("status", "Cold Lead");
        jane.put("notes", "Attended webinar, no follow-up yet.");
        mockCrmData.put("jane@acmecorp.com", jane);

        Map<String, String> bob = new HashMap<>();
        bob.put("last_contact", "2025-12-01");
        bob.put("status", "Active Opportunity");
        bob.put("notes", "Discussed Q1 budget and product integration.");
        mockCrmData.put("bob@widgetco.net", bob);

        Map<String, String> defaultRecord = new HashMap<>();
        defaultRecord.put("last_contact", "N/A");
        defaultRecord.put("status", "No Record");
        defaultRecord.put("notes", "New lead, first contact opportunity.");
        mockCrmData.put("default", defaultRecord);
    }

    public static String lookupDomainInfo(String domain) {
        System.out.println("-> TOOL ACTIVATED: Looking up domain info for " + domain + "...");
        Map<String, String> info = mockDomainData.getOrDefault(domain, 
            Map.of("industry", "Unknown", "size", "N/A", "revenue", "N/A"));
        return gson.toJson(info);
    }

    public static String checkCrmHistory(String email) {
        System.out.println("-> TOOL ACTIVATED: Checking CRM history for " + email + "...");
        Map<String, String> history = mockCrmData.getOrDefault(email, mockCrmData.get("default"));
        return gson.toJson(history);
    }

    public static String calculateLeadScore(String dataSummary) {
        System.out.println("-> TOOL ACTIVATED: Calculating lead score...");
        JsonObject data = gson.fromJson(dataSummary, JsonObject.class);
        String score = "Low";

        try {
            JsonObject domainInfo = data.getAsJsonObject("domain_info");
            JsonObject crmHistory = data.getAsJsonObject("crm_history");

            if (domainInfo != null && domainInfo.has("revenue")) {
                String revenue = domainInfo.get("revenue").getAsString();
                if (revenue.startsWith("$1B+")) {
                    score = "High";
                } else if (revenue.startsWith("$50M")) {
                    score = "Medium";
                }
            }

            if ("Low".equals(score) && crmHistory != null && crmHistory.has("status")) {
                String status = crmHistory.get("status").getAsString();
                if ("Active Opportunity".equals(status)) {
                    score = "High";
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing data for lead score calculation: " + e.getMessage());
        }

        JsonObject result = new JsonObject();
        result.addProperty("lead_score", score);
        return gson.toJson(result);
    }

    public static String callFunction(String functionName, String jsonArgs) {
        JsonObject args = gson.fromJson(jsonArgs, JsonObject.class);

        return switch (functionName) {
            case "lookup_domain_info" -> lookupDomainInfo(args.get("domain").getAsString());
            case "check_crm_history" -> checkCrmHistory(args.get("email").getAsString());
            case "calculate_lead_score" -> calculateLeadScore(args.get("data_summary").getAsString());
            default -> throw new IllegalArgumentException("Unknown function: " + functionName);
        };
    }
}
