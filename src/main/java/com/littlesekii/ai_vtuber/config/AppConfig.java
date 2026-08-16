package com.littlesekii.ai_vtuber.config;

public final class AppConfig {
    private AppConfig() {}

    public static final String OLLAMA_URL = "http://127.0.0.1:11434";

    public static final String AI_MODEL = "llama3.1";

    public static final String TTS_URL = "http://127.0.0.1:8003";

    public static final String VNYAN_WEB_SOCKET = "ws://127.0.0.1:8067/vnyan";

    public static final int MAX_RESPONSE_LENGTH = 200; 
}
