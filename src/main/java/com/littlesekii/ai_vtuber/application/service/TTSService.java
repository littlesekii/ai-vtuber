package com.littlesekii.ai_vtuber.application.service;

import java.nio.file.Path;

import com.littlesekii.ai_vtuber.application.port.out.TTSProviderPort;
import com.littlesekii.ai_vtuber.domain.voice.VoiceEmotion;

public class TTSService {

    private final TTSProviderPort provider;
    private final VoiceEmotion emotion;

    public TTSService(TTSProviderPort provider, VoiceEmotion emotion) {
        this.provider = provider;
        this.emotion = emotion;
    }

    public Path process(String text) {
        System.out.println("[TTS] Generating voice...");
        Path audio = provider.synthesize(text, emotion);
        System.out.println("[TTS] Generated audio: " + audio);
        return audio;
    }
}