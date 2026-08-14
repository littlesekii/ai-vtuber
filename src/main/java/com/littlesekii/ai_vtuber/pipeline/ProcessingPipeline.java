package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.littlesekii.ai_vtuber.core.ai.AIResponse;
import com.littlesekii.ai_vtuber.core.chat.ChatMessage;
import com.littlesekii.ai_vtuber.tts.GeneratedAudio;

public class ProcessingPipeline {
    private final BlockingQueue<ChatMessage> messageQueue = new LinkedBlockingQueue<>(5);
    private final BlockingQueue<AIResponse> responseQueue = new LinkedBlockingQueue<>(3);
    private final BlockingQueue<GeneratedAudio> audioQueue = new LinkedBlockingQueue<>(3);

    public BlockingQueue<ChatMessage> getMessageQueue() {
        return messageQueue;
    }
    public BlockingQueue<AIResponse> getResponseQueue() {
        return responseQueue;
    }
    public BlockingQueue<GeneratedAudio> getAudioQueue() {
        return audioQueue;
    }
}
