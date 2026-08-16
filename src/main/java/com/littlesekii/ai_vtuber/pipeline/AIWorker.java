package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.core.ai.AIResponse;
import com.littlesekii.ai_vtuber.core.ai.AIService;
import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;

public class AIWorker implements Runnable {

    private final BlockingQueue<InteractionEvent> input;
    private final BlockingQueue<AIResponse> output;
    private final AIService aiService;

    public AIWorker(
        BlockingQueue<InteractionEvent> input,
        BlockingQueue<AIResponse> output,
        AIService aiService
    ) {
        this.input = input;
        this.output = output;
        this.aiService = aiService;
    }

    @Override
    public void run() { 
        while (!Thread.currentThread().isInterrupted()) {
            try {
                InteractionEvent interactionEvent = input.take();
                AIResponse response = aiService.process(interactionEvent);
                output.offer(response);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
