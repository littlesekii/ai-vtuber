package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.core.interaction.InteractionService;
import com.littlesekii.ai_vtuber.core.interaction.InteractionType;

public class InteractionWorker implements Runnable {

    private static final int MAX_NORMAL_INTERACTIONS = 5;

    private final BlockingQueue<InteractionEvent> interactionQueue;
    private final InteractionService interactionService;

    public InteractionWorker(
        BlockingQueue<InteractionEvent> interactionQueue,
        InteractionService interactionService
    ) {
        this.interactionQueue = interactionQueue;
        this.interactionService = interactionService;
    } 

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                InteractionEvent interaction = interactionService.listen();

                if (
                    interaction.type() == InteractionType.FOLLOW ||
                    interaction.type() == InteractionType.GIFT
                ) {
                    interactionQueue.put(interaction);
                } else {
                    if (interactionQueue.size() >= MAX_NORMAL_INTERACTIONS) {
                        System.out.println(
                            "[INTERACTION WORKER] Queue busy. Discarding: " + 
                            interaction.username()
                        );
                        continue;
                    }
                    interactionQueue.offer(interaction);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
