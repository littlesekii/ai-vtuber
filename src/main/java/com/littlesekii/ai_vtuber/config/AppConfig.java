package com.littlesekii.ai_vtuber.config;

public final class AppConfig {
    private AppConfig() {}

    public static final String OLLAMA_URL = "http://127.0.0.1:11434";

    public static final String AI_MODEL = "qwen2.5:7b";

    public static final String TTS_URL = "http://127.0.0.1:8003";

    public static final int MAX_RESPONSE_LENGTH = 200; 
}
