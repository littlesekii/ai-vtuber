package com.littlesekii.ai_vtuber.application.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {
    private static AppConfig instance;
    private final Properties props;

    private AppConfig() {
        props = new Properties();
        loadDefaults();
        loadFromFile();
        applyEnvOverrides();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String getString(String key) {
        return props.getProperty(key);
    }

    public String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null)
            return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        String value = props.getProperty(key);
        if (value == null)
            return defaultValue;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null)
            return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    private void loadFromFile() {
        Path filePath = Path.of("config", "ai_vtuber.properties");
        if (Files.exists(filePath)) {
            try (InputStream input = Files.newInputStream(filePath)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("[CONFIG] Warning: Could not load " + filePath + ": " + e.getMessage());
            } 
        }
    }

    private void applyEnvOverrides() {
        for (String key : props.stringPropertyNames()) {
            String envKey = key.replace(".", "_").toUpperCase();

            String envValue = System.getenv(envKey);
            if (envValue != null) {
                props.setProperty(key, envValue);
            }

        }
    }

    private void loadDefaults() {

        // === Input ===
        props.setProperty("app.input.type",
            "tiktok");

        // === Personality ===
        props.setProperty("app.personality.file", 
            "personality/mio.txt");

        // === TikTok ===
        props.setProperty("app.tiktok.streamer-username", 
            "ayamelives");
        props.setProperty("app.tiktok.greeting-chance", 
            "0.3");
        props.setProperty("app.tiktok.follow-thanks-chance", 
            "0.9");
        props.setProperty("app.tiktok.restriction-timeout-seconds", 
            "5");
        props.setProperty("app.tiktok.follow-priority-amount", 
            "1000");
        props.setProperty("app.tiktok.follow-priority-duration-minutes", 
            "2");
        props.setProperty("app.tiktok.gift-priority-multiplier", 
            "500");
        props.setProperty("app.tiktok.gift-priority-duration-minutes", 
            "3");
        props.setProperty("app.tiktok.like-priority-multiplier", 
            "0.5");
        props.setProperty("app.tiktok.like-priority-duration-minutes", 
            "1");
        props.setProperty("app.tiktok.gift-ranking-top-n", 
            "5");
        props.setProperty("app.tiktok.like-ranking-top-n", 
            "5");

        // === AI / Ollama ===
        props.setProperty("app.ollama.url", 
            "http://127.0.0.1:11434");
        props.setProperty("app.ollama.model", 
            "llama3.1");
        props.setProperty("app.ollama.stream", 
            "false");
        props.setProperty("app.ollama.think", 
            "false");
        props.setProperty("app.ollama.repeat-penalty", 
            "1.1");
        props.setProperty("app.ollama.prompt.message-template", 
            "%s Nome: %s | Mensagem: %s");
        props.setProperty("app.ollama.prompt.event-template", 
            "%s Nome: %s | Evento: %s");

        // === TTS ===
        props.setProperty("app.tts.url", 
            "http://127.0.0.1:8003");
        props.setProperty("app.tts.default-emotion",
             "EXPRESSIVE");

        // === Avatar ===     
        props.setProperty("app.avatar.type",
             "vnyan");
        props.setProperty("app.avatar.vnyan.websocket-url", 
            "ws://127.0.0.1:8067/vnyan");

        // === Overlay ===
        props.setProperty("app.overlay.websocket-url", 
            "ws://127.0.0.1:8096/ws");

        // === Audio ===
        props.setProperty("app.audio.mouth-volume-threshold", 
            "0.015");
        props.setProperty("app.audio.polling-interval-ms", 
            "30");

        // === Pipeline ===
        props.setProperty("app.pipeline.thread-pool-size", 
            "4");
        props.setProperty("app.pipeline.interaction-slots", 
            "5");
    }
}

