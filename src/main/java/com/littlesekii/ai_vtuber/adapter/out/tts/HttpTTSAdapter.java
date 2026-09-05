package com.littlesekii.ai_vtuber.adapter.out.tts;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlesekii.ai_vtuber.application.port.out.TTSProviderPort;
import com.littlesekii.ai_vtuber.domain.voice.VoiceEmotion;

public class HttpTTSAdapter implements TTSProviderPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String url;

    public HttpTTSAdapter(String url) {
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
        this.objectMapper = new ObjectMapper();
        this.url = url;
    }

    @Override
    public Path synthesize(String text, VoiceEmotion emotion) {
        try {

            String body = objectMapper.createObjectNode()
                .put("text", text)
                .put("emotion", emotion.name())
                .toString();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/tts"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                    "TTS Server error. HTTP " + 
                    response.statusCode() + 
                    ": " + 
                    response.body()
                );
            }

            JsonNode json = objectMapper.readTree(
                response.body()
            );

            String file = json.path("file").asText();

            if (file.isBlank()) {
                throw new RuntimeException("TTS Server doesn't returned generated file.");
            }

            return Path.of(file);

        } catch (IOException e) {
            throw new RuntimeException("TTS Server communication failed.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("TTS generation was interrupted.", e);
        }
    }
    
}
