package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

import com.littlesekii.ai_vtuber.core.interaction.InteractionData;

public class ProcessingPipeline {
    private final BlockingQueue<InteractionData> aiProcessingQueue = new PriorityBlockingQueue<>();
    private final BlockingQueue<InteractionData> ttsQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<InteractionData> outputQueue = new LinkedBlockingQueue<>();

    private final Semaphore interactionSlots = new Semaphore(5);

    public BlockingQueue<InteractionData> getAIProcessingQueue() {
        return aiProcessingQueue;
    }
    public BlockingQueue<InteractionData> getTTSQueue() {
        return ttsQueue;
    }
    public BlockingQueue<InteractionData> getOutputQueue() {
        return outputQueue;
    }

    public Semaphore getInteractionSlots() {
        return interactionSlots;
    }
}
