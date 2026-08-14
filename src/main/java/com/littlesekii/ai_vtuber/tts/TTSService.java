package com.littlesekii.ai_vtuber.tts;

import java.nio.file.Path;

import com.littlesekii.ai_vtuber.core.ai.AIResponse;

public class TTSService {

    private final TTSProvider provider;

    public TTSService(TTSProvider provider) {
        this.provider = provider;
    }

    public GeneratedAudio process(AIResponse response) {

        System.out.println("[TTS] Generating voice...");

        Path audio = provider.synthesize(
            response.response(),
            VoiceEmotion.EXPRESSIVE
        );

        System.out.println("[TTS] Generated audio: " + audio);
        
        return new GeneratedAudio(audio);
    }
}