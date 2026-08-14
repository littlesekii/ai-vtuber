package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.core.ai.AIResponse;
import com.littlesekii.ai_vtuber.tts.GeneratedAudio;
import com.littlesekii.ai_vtuber.tts.TTSService;

public class TTSWorker implements Runnable {

    private final BlockingQueue<AIResponse> input;
    private final BlockingQueue<GeneratedAudio> output;
    private final TTSService ttsService;

    public TTSWorker(
        BlockingQueue<AIResponse> input,
        BlockingQueue<GeneratedAudio> output,
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
                AIResponse response = input.take();
                GeneratedAudio audio = ttsService.process(response);
                output.offer(audio);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
