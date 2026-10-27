package com.crmleadqualifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;
import com.google.gson.Gson;

public class OpenAIConnector {
    private static final String API_KEY = "";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient();

    public static JsonObject callOpenAI(String model, JsonArray messages, JsonArray tools) throws Exception {
        System.out.println("\n--- Calling OpenAI API ---");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.add("messages", messages);
        requestBody.add("tools", tools);
        requestBody.addProperty("tool_choice", "auto");

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("API call failed: " + response.code() + " " + response.body().string());
            }

            String responseBody = response.body().string();
            return gson.fromJson(responseBody, JsonObject.class);
        }
    }
}
