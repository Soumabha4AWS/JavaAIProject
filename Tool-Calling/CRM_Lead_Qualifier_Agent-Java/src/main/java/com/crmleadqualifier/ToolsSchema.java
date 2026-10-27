package com.crmleadqualifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ToolsSchema {
    public static JsonArray getToolsSchema() {
        JsonArray tools = new JsonArray();

        // lookup_domain_info tool
        JsonObject lookupDomainTool = new JsonObject();
        lookupDomainTool.addProperty("type", "function");
        JsonObject lookupDomainFunc = new JsonObject();
        lookupDomainFunc.addProperty("name", "lookup_domain_info");
        lookupDomainFunc.addProperty("description", 
            "Retrieves general business information (industry, size, revenue) about a company based on its domain name.");
        
        JsonObject lookupDomainParams = new JsonObject();
        lookupDomainParams.addProperty("type", "object");
        JsonObject lookupDomainProps = new JsonObject();
        JsonObject domainProp = new JsonObject();
        domainProp.addProperty("type", "string");
        domainProp.addProperty("description", "The company's domain name, e.g., 'acmecorp.com'");
        lookupDomainProps.add("domain", domainProp);
        lookupDomainParams.add("properties", lookupDomainProps);
        JsonArray lookupDomainRequired = new JsonArray();
        lookupDomainRequired.add("domain");
        lookupDomainParams.add("required", lookupDomainRequired);
        
        lookupDomainFunc.add("parameters", lookupDomainParams);
        lookupDomainTool.add("function", lookupDomainFunc);
        tools.add(lookupDomainTool);

        // check_crm_history tool
        JsonObject checkCrmTool = new JsonObject();
        checkCrmTool.addProperty("type", "function");
        JsonObject checkCrmFunc = new JsonObject();
        checkCrmFunc.addProperty("name", "check_crm_history");
        checkCrmFunc.addProperty("description", 
            "Checks the internal CRM system for past contact, status, and notes associated with a specific lead email.");
        
        JsonObject checkCrmParams = new JsonObject();
        checkCrmParams.addProperty("type", "object");
        JsonObject checkCrmProps = new JsonObject();
        JsonObject emailProp = new JsonObject();
        emailProp.addProperty("type", "string");
        emailProp.addProperty("description", "The full email address of the lead.");
        checkCrmProps.add("email", emailProp);
        checkCrmParams.add("properties", checkCrmProps);
        JsonArray checkCrmRequired = new JsonArray();
        checkCrmRequired.add("email");
        checkCrmParams.add("required", checkCrmRequired);
        
        checkCrmFunc.add("parameters", checkCrmParams);
        checkCrmTool.add("function", checkCrmFunc);
        tools.add(checkCrmTool);

        // calculate_lead_score tool
        JsonObject scoreToolDef = new JsonObject();
        scoreToolDef.addProperty("type", "function");
        JsonObject scoreFunc = new JsonObject();
        scoreFunc.addProperty("name", "calculate_lead_score");
        scoreFunc.addProperty("description", 
            "Calculates the priority score (High/Medium/Low) for a lead based on a summary of all collected domain and CRM history data.");
        
        JsonObject scoreParams = new JsonObject();
        scoreParams.addProperty("type", "object");
        JsonObject scoreProps = new JsonObject();
        JsonObject summaryProp = new JsonObject();
        summaryProp.addProperty("type", "string");
        summaryProp.addProperty("description", "A JSON string containing the combined domain_info and crm_history.");
        scoreProps.add("data_summary", summaryProp);
        scoreParams.add("properties", scoreProps);
        JsonArray scoreRequired = new JsonArray();
        scoreRequired.add("data_summary");
        scoreParams.add("required", scoreRequired);
        
        scoreFunc.add("parameters", scoreParams);
        scoreToolDef.add("function", scoreFunc);
        tools.add(scoreToolDef);

        return tools;
    }
}
