package com.littlesekii.ai_vtuber.application;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.littlesekii.ai_vtuber.adapter.in.InteractionProviderFactory;
import com.littlesekii.ai_vtuber.adapter.out.ai.ollama.OllamaAIAdapter;
import com.littlesekii.ai_vtuber.adapter.out.audio.AudioPlayerAdapter;
import com.littlesekii.ai_vtuber.adapter.out.overlay.OverlayAdapter;
import com.littlesekii.ai_vtuber.adapter.out.overlay.OverlayWebSocketClient;
import com.littlesekii.ai_vtuber.adapter.out.personality.FilePersonalityAdapter;
import com.littlesekii.ai_vtuber.adapter.out.tts.HttpTTSAdapter;
import com.littlesekii.ai_vtuber.adapter.out.vnyan.VNyanAvatarAdapter;
import com.littlesekii.ai_vtuber.adapter.out.vnyan.VNyanWebSocketClient;
import com.littlesekii.ai_vtuber.application.pipeline.AIWorker;
import com.littlesekii.ai_vtuber.application.pipeline.InteractionWorker;
import com.littlesekii.ai_vtuber.application.pipeline.OutputWorker;
import com.littlesekii.ai_vtuber.application.pipeline.ProcessingPipeline;
import com.littlesekii.ai_vtuber.application.pipeline.TTSWorker;
import com.littlesekii.ai_vtuber.application.port.in.InteractionProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.AIProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.AudioPlayerProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.PersonalityProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.TTSProviderPort;
import com.littlesekii.ai_vtuber.application.service.AIService;
import com.littlesekii.ai_vtuber.application.service.InteractionService;
import com.littlesekii.ai_vtuber.application.service.OutputService;
import com.littlesekii.ai_vtuber.application.service.TTSService;
import com.littlesekii.ai_vtuber.domain.voice.VoiceEmotion;
import com.littlesekii.ai_vtuber.infra.config.AppConfig;

public class AIVtuberApplication {

    private final VNyanWebSocketClient vnyanWSClient;
    private final VNyanAvatarAdapter avatar;

    private final OverlayWebSocketClient overlayWSClient;
    private final OverlayAdapter overlay;

    private final InteractionService interactionService;
    private final AIService aiService;
    private final TTSService ttsService; 
    private final OutputService outputService;

    private final ProcessingPipeline pipeline;
    private final ExecutorService executor;

    public AIVtuberApplication() {
        AppConfig config = AppConfig.getInstance();

        vnyanWSClient = new VNyanWebSocketClient(
            config.getString("app.avatar.vnyan.websocket-url")
        );
        avatar = new VNyanAvatarAdapter(vnyanWSClient);

        overlayWSClient = new OverlayWebSocketClient(
            config.getString("app.overlay.websocket-url")
        );
        overlay = new OverlayAdapter(overlayWSClient);

        InteractionProviderPort interactionProviderPort = InteractionProviderFactory.create(config, overlay);
        interactionService = new InteractionService(interactionProviderPort);

        TTSProviderPort ttsProviderPort = new HttpTTSAdapter(config.getString("app.tts.url"));
        ttsService = new TTSService(
            ttsProviderPort,
            VoiceEmotion.valueOf(
                config.getString("app.tts.default-emotion", 
                    "EXPRESSIVE")
            )
        );
        
        AIProviderPort aiProviderPort = new OllamaAIAdapter(
            config.getString("app.ollama.url"),
            config.getString("app.ollama.model"),
            config.getBoolean("app.ollama.stream", false),
            config.getBoolean("app.ollama.think", false),
            config.getDouble("app.ollama.repeat-penalty", 1.1),
            config.getString("app.ollama.prompt.message-template"),
            config.getString("app.ollama.prompt.event-template")
        );
        PersonalityProviderPort personalityProviderPort = new FilePersonalityAdapter( 
            Path.of(config.getString("app.personality.file"))
        );
        aiService = new AIService(aiProviderPort, personalityProviderPort);
        
        AudioPlayerProviderPort audioPlayerProviderPort = new AudioPlayerAdapter(
            avatar,
            config.getDouble("app.audio.mouth-volume-threshold", 0.015),
            config.getInt("app.audio.polling-interval-ms", 30)
        );
        outputService = new OutputService(
            avatar,
            overlay,
            audioPlayerProviderPort
        );
        
        pipeline = new ProcessingPipeline(
            config.getInt("app.pipeline.interaction-slots", 5)
        );
        executor = Executors.newFixedThreadPool(
            config.getInt("app.pipeline.thread-pool-size", 4)
        );
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
            aiService,
            pipeline.getInteractionSlots()
        ));
        executor.submit(new TTSWorker(
            pipeline.getTTSQueue(), 
            pipeline.getOutputQueue(), 
            ttsService,
            pipeline.getInteractionSlots()
        ));
        executor.submit(new OutputWorker(
            pipeline.getOutputQueue(),
            outputService,
            pipeline.getInteractionSlots()
        ));  
    }
}
