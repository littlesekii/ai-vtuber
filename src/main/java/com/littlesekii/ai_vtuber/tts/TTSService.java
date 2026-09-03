package com.littlesekii.ai_vtuber.tts;

import java.nio.file.Path;

public class TTSService {

    private final TTSProvider provider;

    public TTSService(TTSProvider provider) {
        this.provider = provider;
    }

    public Path process(String text) {
        System.out.println("[TTS] Generating voice...");
        Path audio = provider.synthesize(text, VoiceEmotion.EXPRESSIVE);
        System.out.println("[TTS] Generated audio: " + audio);
        return audio;
    }
}