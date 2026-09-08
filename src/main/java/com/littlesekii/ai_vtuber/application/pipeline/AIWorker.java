package com.littlesekii.ai_vtuber.application.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.littlesekii.ai_vtuber.application.model.InteractionData;
import com.littlesekii.ai_vtuber.application.service.AIService;

public class AIWorker implements Runnable {

    private final BlockingQueue<InteractionData> input;
    private final BlockingQueue<InteractionData> output;
    private final AIService aiService;
    private final Semaphore interactionSlots;

    public AIWorker(
        BlockingQueue<InteractionData> input,
        BlockingQueue<InteractionData> output,
        AIService aiService,
        Semaphore interactionSlots
    ) {
        this.input = input;
        this.output = output;
        this.aiService = aiService;
        this.interactionSlots = interactionSlots;
    }

    @Override
    public void run() { 
        while (!Thread.currentThread().isInterrupted()) {
            try {
                InteractionData interactionData = input.take();

                try {
                    String response = aiService.generateResponse(interactionData.getInteractionEvent());
                    interactionData.setAIResponse(response);                            
                    output.offer(interactionData);
                } catch (Exception e) {
                    System.err.println("[AI WORKER] Error generating response: " + e.getMessage());
                    if (interactionData.getInteractionSlot()) {
                        interactionSlots.release();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
