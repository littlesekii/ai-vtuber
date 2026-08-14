package com.littlesekii.ai_vtuber.tts;

import java.nio.file.Path;

public interface TTSProvider {

    Path synthesize(
        String text,
        VoiceEmotion emotion
    );

}