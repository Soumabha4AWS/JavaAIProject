package com.crmleadqualifier;

import com.crmleadqualifier.module.CrmLeadQualifierModule1;
import com.crmleadqualifier.module.CrmLeadQualifierModule2;

public class CrmLeadQualifierMain {
    public static void main(String[] args) {
        try {
            String leadEmail1 = "jane@acmecorp.com";
            System.out.println("\n" + "=".repeat(80) + "\n");
            CrmLeadQualifierModule1.runAgent("Please qualify this lead for my call tomorrow: " + leadEmail1);
            System.out.println("\n" + "+".repeat(80) + "\n");
            CrmLeadQualifierModule2.runAgent("Please qualify this lead for my call tomorrow: " + leadEmail1);
            System.out.println("\n" + "=".repeat(80) + "\n");

            String leadEmail2 = "bob@widgetco.net";
            CrmLeadQualifierModule1.runAgent("Can you run an analysis on this lead: " + leadEmail2);
            System.out.println("\n" + "+".repeat(80) + "\n");
            CrmLeadQualifierModule2.runAgent("Can you run an analysis on this lead: " + leadEmail2);
            System.out.println("\n" + "=".repeat(80) + "\n");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
