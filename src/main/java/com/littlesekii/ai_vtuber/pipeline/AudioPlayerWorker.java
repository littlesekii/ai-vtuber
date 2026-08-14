package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.audio.AudioPlayerService;
import com.littlesekii.ai_vtuber.tts.GeneratedAudio;

public class AudioPlayerWorker implements Runnable {

    private final BlockingQueue<GeneratedAudio> input;
    private final AudioPlayerService audioPlayerService;

    public AudioPlayerWorker(
        BlockingQueue<GeneratedAudio> input,
         AudioPlayerService audioPlayerService
    ) {
        this.input = input;
        this.audioPlayerService = audioPlayerService;
    }

    @Override
    public void run() {
       while (!Thread.currentThread().isInterrupted()) {
            try {
                GeneratedAudio audio = input.take();
                audioPlayerService.process(audio);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
       }
    }
}
