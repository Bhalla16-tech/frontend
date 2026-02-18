package com.kinovek.backend.service;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    private OkHttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        System.out.println("✅ Gemini AI Service initialized with model: " + model);
    }

    public String generateResponse(String systemPrompt, String userPrompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

            String requestBody = buildRequestBody(systemPrompt, userPrompt);

            Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    System.err.println("Gemini API error: " + response.code() + " - " + errorBody);
                    throw new RuntimeException("Gemini API error: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JsonNode root = mapper.readTree(responseBody);

                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).path("text").asText();
                    }
                }
                throw new RuntimeException("No content in Gemini response. Full response: " + responseBody);
            }
        } catch (IOException e) {
            System.err.println("Failed to call Gemini API: " + e.getMessage());
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    public String generateJsonResponse(String systemPrompt, String userPrompt) {
        String response = generateResponse(systemPrompt, userPrompt);
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        response = response.trim();
        int firstBrace = response.indexOf('{');
        int lastBrace = response.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            response = response.substring(firstBrace, lastBrace + 1);
        }
        return response;
    }

    private String buildRequestBody(String systemPrompt, String userPrompt) {
        try {
            ObjectMapper m = new ObjectMapper();
            var root = m.createObjectNode();

            var systemInstruction = m.createObjectNode();
            var systemParts = m.createArrayNode();
            systemParts.add(m.createObjectNode().put("text", systemPrompt));
            systemInstruction.set("parts", systemParts);
            root.set("system_instruction", systemInstruction);

            var contents = m.createArrayNode();
            var userContent = m.createObjectNode();
            userContent.put("role", "user");
            var userParts = m.createArrayNode();
            userParts.add(m.createObjectNode().put("text", userPrompt));
            userContent.set("parts", userParts);
            contents.add(userContent);
            root.set("contents", contents);

            var genConfig = m.createObjectNode();
            genConfig.put("temperature", 0.3);
            genConfig.put("maxOutputTokens", 8192);
            root.set("generationConfig", genConfig);

            return m.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request body: " + e.getMessage(), e);
        }
    }
}
