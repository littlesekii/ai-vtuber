package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.littlesekii.ai_vtuber.core.interaction.InteractionData;
import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.core.interaction.InteractionService;
import com.littlesekii.ai_vtuber.core.interaction.InteractionType;

public class InteractionWorker implements Runnable {
    
    private final BlockingQueue<InteractionData> output;
    private final InteractionService interactionService;
    private final Semaphore interactionSlots;

    public InteractionWorker(
        BlockingQueue<InteractionData> output,
        InteractionService interactionService,
        Semaphore interactionSlots
    ) {
        this.output = output;
        this.interactionService = interactionService;
        this.interactionSlots = interactionSlots;
    } 

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                InteractionEvent interaction = interactionService.listen();

                InteractionData interactionData = new InteractionData();
                interactionData.setInteractionEvent(interaction);

                if (
                    interaction.type() == InteractionType.FOLLOW ||
                    interaction.type() == InteractionType.GIFT
                ) {
                    output.offer(interactionData);
                    continue;
                }

                if (!interactionSlots.tryAcquire()) {
                    System.out.println(
                        "[INTERACTION WORKER] Pipeline busy. Discarding: " +
                        interaction.username() +
                        ": " +
                        interaction.body()
                    );
                    continue;
                }

                interactionData.setInteractionSlot(true);

                output.offer(interactionData);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
