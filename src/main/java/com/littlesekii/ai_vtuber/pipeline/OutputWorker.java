package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.littlesekii.ai_vtuber.core.interaction.InteractionData;
import com.littlesekii.ai_vtuber.output.OutputService;

public class OutputWorker implements Runnable {

    private final BlockingQueue<InteractionData> input;
    private final OutputService outputService;

    private final Semaphore interactionSlots;

    public OutputWorker(
        BlockingQueue<InteractionData> input,
        OutputService outputService,
        Semaphore interactionSlots
    ) {
        this.input = input;
        this.outputService = outputService;
        this.interactionSlots = interactionSlots;
    }

    @Override
    public void run() {
       while (!Thread.currentThread().isInterrupted()) {
            try {
                InteractionData interactionData = input.take();

                try {
                    outputService.process(interactionData);

                } finally {
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
