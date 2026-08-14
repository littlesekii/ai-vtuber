package com.littlesekii.ai_vtuber.application;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.littlesekii.ai_vtuber.audio.AudioPlayerService;
import com.littlesekii.ai_vtuber.avatar.VnyanAvatarController;
import com.littlesekii.ai_vtuber.core.ai.AIProvider;
import com.littlesekii.ai_vtuber.core.ai.AIService;
import com.littlesekii.ai_vtuber.core.ai.OllamaAIProvider;
import com.littlesekii.ai_vtuber.core.chat.ChatService;
import com.littlesekii.ai_vtuber.core.personality.PersonalityService;
import com.littlesekii.ai_vtuber.pipeline.AIWorker;
import com.littlesekii.ai_vtuber.pipeline.AudioPlayerWorker;
import com.littlesekii.ai_vtuber.pipeline.ChatWorker;
import com.littlesekii.ai_vtuber.pipeline.ProcessingPipeline;
import com.littlesekii.ai_vtuber.pipeline.TTSWorker;
import com.littlesekii.ai_vtuber.tts.HttpTTSProvider;
import com.littlesekii.ai_vtuber.tts.TTSProvider;
import com.littlesekii.ai_vtuber.tts.TTSService;

public class AIVtuberApplication {

    private final ChatService chatService;
    private final TTSService ttsService;
    private final AIService aiService;
    private final AudioPlayerService audioPlayerService;

    private final ProcessingPipeline pipeline;
    private final ExecutorService executor;

    public AIVtuberApplication() {
        chatService = new ConsoleChatService();
        // chatService = new TiktokChatService("gostosinhado_");
        
        TTSProvider ttsProvider = new HttpTTSProvider();
        ttsService = new TTSService(ttsProvider);
        
        AIProvider aiProvider = new OllamaAIProvider();
        PersonalityService personalityService = new PersonalityService(
            Path.of("personality", "sakura.txt")
        );
        aiService = new AIService(aiProvider, personalityService);
        
        audioPlayerService = new AudioPlayerService(new VnyanAvatarController());
        
        pipeline = new ProcessingPipeline();
        executor = Executors.newFixedThreadPool(4);

    }

    public void start() {
        executor.submit(new ChatWorker(
            pipeline.getMessageQueue(), 
            chatService
        ));
        executor.submit(new AIWorker(
            pipeline.getMessageQueue(), 
            pipeline.getResponseQueue(), 
            aiService
        ));
        executor.submit(new TTSWorker(
            pipeline.getResponseQueue(), 
            pipeline.getAudioQueue(), 
            ttsService
        ));
        executor.submit(new AudioPlayerWorker(
            pipeline.getAudioQueue(),
            audioPlayerService
        ));  
    }
}
