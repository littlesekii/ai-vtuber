package com.littlesekii.ai_vtuber.pipeline;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.core.interaction.InteractionData;
import com.littlesekii.ai_vtuber.tts.TTSService;

public class TTSWorker implements Runnable {

    private final BlockingQueue<InteractionData> input;
    private final BlockingQueue<InteractionData> output;
    private final TTSService ttsService;

    public TTSWorker(
        BlockingQueue<InteractionData> input,
        BlockingQueue<InteractionData> output,
        TTSService ttsService
    ) {
        this.input = input;
        this.output = output;
        this.ttsService = ttsService;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                InteractionData interactionData = input.take();

                Path audio = ttsService.process(interactionData.getAIResponse());
                interactionData.setTTSAudio(audio);
                
                output.offer(interactionData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
