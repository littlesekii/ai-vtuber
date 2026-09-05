package com.littlesekii.ai_vtuber.adapter.out.ai.ollama;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.littlesekii.ai_vtuber.application.port.out.AIProviderPort;

public class OllamaAIAdapter implements AIProviderPort {

    private final String url;
    private final String model;
    private final boolean enableStream;
    private final boolean enableThinking;
    private final double repeatPenalty;
    private final String promptMessageTemplate;
    private final String promptEventTemplate;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaAIAdapter(
        String url,
        String model, 
        boolean enableStream,
        boolean enableThinking,
        double repeatPenalty,
        String promptMessageTemplate,
        String promptEventTemplate
    ) {
        this.url = url;
        this.model = model;
        this.enableStream = enableStream;
        this.enableThinking = enableThinking;
        this.repeatPenalty = repeatPenalty;
        this.promptMessageTemplate = promptMessageTemplate;
        this.promptEventTemplate = promptEventTemplate;

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateMessageResponse(String context, String username, String message) {
        String prompt = String.format(promptMessageTemplate, context, username, message);
        return generateResponse(prompt);      
    }

    @Override
    public String generateEventResponse(String context, String username, String event) {
        String prompt = String.format(promptEventTemplate, context, username, event);
        return generateResponse(prompt);      
    }

    private String generateResponse(String prompt) {

        ObjectNode body = objectMapper.createObjectNode();

        body.put("model", model);
        body.put("prompt", prompt);
        body.put("stream", enableStream);
        body.put("think", enableThinking);
        // body.put("keep_alive", "30m");

        ObjectNode options = body.putObject("options");

        // // options.put("temperature", 0.9);
        // // options.put("top_p", 0.9);
        options.put("repeat_penalty", repeatPenalty);
        // // options.put("num_predict", 150);

        String json = body.toString();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url + "/api/generate"))
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
