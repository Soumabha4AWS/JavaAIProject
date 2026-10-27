package com.openai;

public class OpenAIConnectMain {
    public static void main(String[] args) {
        String testPrompt = "Explain gpt-4o-mini model in one line";

        OpenAIConnectModule.queryOpenAI(testPrompt)
                .thenAccept(result -> System.out.println(result))
                .exceptionally(error -> {
                    System.err.println("Error: " + error.getMessage());
                    return null;
                })
                .join();
    }
}
