package com.littlesekii.ai_vtuber.application.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.littlesekii.ai_vtuber.application.model.InteractionData;
import com.littlesekii.ai_vtuber.application.port.in.InteractionProviderPort;
import com.littlesekii.ai_vtuber.application.service.InteractionService;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionType;

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
