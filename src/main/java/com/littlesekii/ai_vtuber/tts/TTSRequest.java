package com.littlesekii.ai_vtuber.tts;

public record TTSRequest(
        String text,
        VoiceEmotion emotion
) {
}