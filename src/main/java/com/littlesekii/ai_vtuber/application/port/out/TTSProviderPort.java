package com.littlesekii.ai_vtuber.application.port.out;

import java.nio.file.Path;

import com.littlesekii.ai_vtuber.domain.voice.VoiceEmotion;

public interface TTSProviderPort {

    Path synthesize(
        String text,
        VoiceEmotion emotion
    );

}