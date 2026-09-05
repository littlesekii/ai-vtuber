package com.littlesekii.ai_vtuber.application.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

import com.littlesekii.ai_vtuber.application.model.InteractionData;

public class ProcessingPipeline {
    private final BlockingQueue<InteractionData> aiProcessingQueue;
    private final BlockingQueue<InteractionData> ttsQueue;
    private final BlockingQueue<InteractionData> outputQueue;

    private final Semaphore interactionSlots;

    public ProcessingPipeline(int interactionSlots) {
        this.aiProcessingQueue = new PriorityBlockingQueue<>();
        this.ttsQueue = new LinkedBlockingQueue<>();
        this.outputQueue = new LinkedBlockingQueue<>();
        this.interactionSlots = new Semaphore(interactionSlots);
    }

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
