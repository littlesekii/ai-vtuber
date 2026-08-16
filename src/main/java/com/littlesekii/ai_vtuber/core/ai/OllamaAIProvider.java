package com.littlesekii.ai_vtuber.core.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.littlesekii.ai_vtuber.config.AppConfig;

public class OllamaAIProvider implements AIProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaAIProvider() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateMessageResponse(String context, String username, String message) {
        String prompt = context + " Nome: " + username + " | Mensagem: " + message;
        // System.out.println(prompt);
        return generateResponse(prompt);      
    }

    @Override
    public String generateEventResponse(String context, String username, String event) {
        String prompt = context + " Nome: " + username + " | Evento: " + event;
        // System.out.println(prompt);
        return generateResponse(prompt);      
    }

    private String generateResponse(String prompt) {

        ObjectNode body = objectMapper.createObjectNode();

        body.put("model", AppConfig.AI_MODEL);
        body.put("prompt", prompt);
        body.put("stream", false);
        body.put("think", false);
        // body.put("keep_alive", "30m");

        ObjectNode options = body.putObject("options");

        // // options.put("temperature", 0.9);
        // // options.put("top_p", 0.9);
        options.put("repeat_penalty", 1.1);
        // // options.put("num_predict", 150);

        String json = body.toString();

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
}
