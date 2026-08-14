package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.core.ai.AIResponse;
import com.littlesekii.ai_vtuber.core.ai.AIService;
import com.littlesekii.ai_vtuber.core.chat.ChatMessage;

public class AIWorker implements Runnable {

    private final BlockingQueue<ChatMessage> input;
    private final BlockingQueue<AIResponse> output;
    private final AIService aiService;

    public AIWorker(
        BlockingQueue<ChatMessage> input,
        BlockingQueue<AIResponse> output,
        AIService aiService
    ) {
        this.input = input;
        this.output = output;
        this.aiService = aiService;
    }

    @Override
    public void run() { 
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ChatMessage message = input.take();
                AIResponse response = aiService.process(message);
                output.offer(response);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
