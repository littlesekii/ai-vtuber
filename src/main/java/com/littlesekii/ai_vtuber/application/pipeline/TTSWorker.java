package com.littlesekii.ai_vtuber.application.pipeline;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.littlesekii.ai_vtuber.application.model.InteractionData;
import com.littlesekii.ai_vtuber.application.service.TTSService;

public class TTSWorker implements Runnable {

    private final BlockingQueue<InteractionData> input;
    private final BlockingQueue<InteractionData> output;
    private final TTSService ttsService;
    private final Semaphore interactionSlots;

    public TTSWorker(
        BlockingQueue<InteractionData> input,
        BlockingQueue<InteractionData> output,
        TTSService ttsService,
        Semaphore interactionSlots
    ) {
        this.input = input;
        this.output = output;
        this.ttsService = ttsService;
        this.interactionSlots = interactionSlots;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                InteractionData interactionData = input.take();

                try {
                    Path audio = ttsService.process(interactionData.getAIResponse());
                    interactionData.setTTSAudio(audio);                
                    output.offer(interactionData);
                } catch (Exception e) {
                    System.err.println("[TTS WORKER] Error generating audio: " + e.getMessage());
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
