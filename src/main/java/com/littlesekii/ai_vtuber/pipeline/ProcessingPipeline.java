package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import com.littlesekii.ai_vtuber.core.ai.AIResponse;
import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.tts.GeneratedAudio;

public class ProcessingPipeline {
    private final BlockingQueue<InteractionEvent> interactionQueue = new PriorityBlockingQueue<>();
    private final BlockingQueue<AIResponse> responseQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<GeneratedAudio> audioQueue = new LinkedBlockingQueue<>();

    public BlockingQueue<InteractionEvent> getInteractionQueue() {
        return interactionQueue;
    }
    public BlockingQueue<AIResponse> getResponseQueue() {
        return responseQueue;
    }
    public BlockingQueue<GeneratedAudio> getAudioQueue() {
        return audioQueue;
    }
}
