package com.littlesekii.ai_vtuber.application.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import com.littlesekii.ai_vtuber.application.model.InteractionData;
import com.littlesekii.ai_vtuber.application.service.OutputService;

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
                    long sleepTime = (long) (ThreadLocalRandom.current().nextDouble() * 1000);
                    Thread.sleep(sleepTime);
                    System.out.println(sleepTime);

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
