package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.core.ai.AIService;
import com.littlesekii.ai_vtuber.core.interaction.InteractionData;

public class AIWorker implements Runnable {

    private final BlockingQueue<InteractionData> input;
    private final BlockingQueue<InteractionData> output;
    private final AIService aiService;

    public AIWorker(
        BlockingQueue<InteractionData> input,
        BlockingQueue<InteractionData> output,
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
                InteractionData interactionData = input.take();

                String response = aiService.process(interactionData.getInteractionEvent());
                interactionData.setAIResponse(response);
                // Thread.sleep(1000);
                

                output.offer(interactionData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
