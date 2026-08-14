package com.littlesekii.ai_vtuber.core.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlesekii.ai_vtuber.config.AppConfig;

public class OllamaAIProvider implements AIProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaAIProvider() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateResponse(String context, String username, String message) {
        String prompt =
            context + "\n\n" + 
            "Nome: " + username + 
            "\nMensagem : " + message;

        String json =
                "{"
                + "\"model\":\"" + AppConfig.AI_MODEL + "\","
                + "\"prompt\":\"" + escapeJson(prompt) + "\","
                + "\"stream\":false"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(AppConfig.OLLAMA_URL + "/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

            try {
                HttpResponse<String> response = httpClient.send(
                    request, 
                    HttpResponse.BodyHandlers.ofString()
                );
                
                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                        "Ollama retornou HTTP " +
                        response.statusCode() +
                        ": " +
                        response.body()
                    );
                }

                JsonNode jsonResponse = objectMapper.readTree(
                    response.body()
                );

                return jsonResponse.path("response").asText();
                
            } catch (IOException | InterruptedException e) {

            throw new RuntimeException(
                "Ollama communication failed",
                e
            );
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
    
}
