package com.littlesekii.ai_vtuber.application;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.littlesekii.ai_vtuber.avatar.VNyanAvatarController;
import com.littlesekii.ai_vtuber.avatar.VNyanWebSocketClient;
import com.littlesekii.ai_vtuber.core.ai.AIProvider;
import com.littlesekii.ai_vtuber.core.ai.AIService;
import com.littlesekii.ai_vtuber.core.ai.OllamaAIProvider;
import com.littlesekii.ai_vtuber.core.interaction.InteractionService;
import com.littlesekii.ai_vtuber.core.personality.PersonalityService;
import com.littlesekii.ai_vtuber.core.priority.PriorityService;
import com.littlesekii.ai_vtuber.output.OutputService;
import com.littlesekii.ai_vtuber.overlay.OverlayController;
import com.littlesekii.ai_vtuber.overlay.OverlayWebSocketClient;
import com.littlesekii.ai_vtuber.pipeline.AIWorker;
import com.littlesekii.ai_vtuber.pipeline.InteractionWorker;
import com.littlesekii.ai_vtuber.pipeline.OutputWorker;
import com.littlesekii.ai_vtuber.pipeline.ProcessingPipeline;
import com.littlesekii.ai_vtuber.pipeline.TTSWorker;
import com.littlesekii.ai_vtuber.tts.HttpTTSProvider;
import com.littlesekii.ai_vtuber.tts.TTSProvider;
import com.littlesekii.ai_vtuber.tts.TTSService;

public class AIVtuberApplication {

    private final VNyanWebSocketClient vnyanWSClient;
    private final VNyanAvatarController vnyanAvatar;

    private final OverlayWebSocketClient overlayWSClient;
    private final OverlayController overlay;

    private final PriorityService priorityService;
    private final InteractionService interactionService;
    private final TTSService ttsService;
    private final AIService aiService;
    private final OutputService outputService;

    private final ProcessingPipeline pipeline;
    private final ExecutorService executor;

    public AIVtuberApplication() {

        vnyanWSClient = new VNyanWebSocketClient();
        vnyanAvatar = new VNyanAvatarController(vnyanWSClient);

        overlayWSClient = new OverlayWebSocketClient();
        overlay = new OverlayController(overlayWSClient);

        priorityService = new PriorityService();

        interactionService = new ConsoleInteractionService();
        // interactionService = new TiktokInteractionService(
        //     "ayamelives", 
        //     priorityService,
        //     overlay
        // );
        
        TTSProvider ttsProvider = new HttpTTSProvider();
        ttsService = new TTSService(ttsProvider);
        
        AIProvider aiProvider = new OllamaAIProvider();
        PersonalityService personalityService = new PersonalityService(
            Path.of("personality", "mio.txt")
        );
        aiService = new AIService(aiProvider, personalityService);
        
        outputService = new OutputService(vnyanAvatar, overlay);
        
        pipeline = new ProcessingPipeline();
        executor = Executors.newFixedThreadPool(4);
    }

    public void start() {
        executor.submit(new InteractionWorker(
            pipeline.getAIProcessingQueue(), 
            interactionService,
            pipeline.getInteractionSlots()
        ));
        executor.submit(new AIWorker(
            pipeline.getAIProcessingQueue(), 
            pipeline.getTTSQueue(), 
            aiService
        ));
        executor.submit(new TTSWorker(
            pipeline.getTTSQueue(), 
            pipeline.getOutputQueue(), 
            ttsService
        ));
        executor.submit(new OutputWorker(
            pipeline.getOutputQueue(),
            outputService,
            pipeline.getInteractionSlots()
        ));  
    }
}
