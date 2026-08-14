package com.littlesekii.ai_vtuber.core.ai;

import com.littlesekii.ai_vtuber.core.chat.ChatMessage;
import com.littlesekii.ai_vtuber.core.personality.PersonalityService;

public class AIService {
    private final AIProvider provider;
    private final PersonalityService personalityService;

    public AIService(
        AIProvider provider,
        PersonalityService personalityService
    ) {
        this.provider = provider;
        this.personalityService = personalityService;
    }

    public AIResponse process(ChatMessage message) {
        System.out.println("[AI] Generating response...");
        String response = provider.generateResponse(
            personalityService.getPersonality(),
            message.username(),
            message.message()
        );
        System.out.println("[AI] Generated response: " + response);

        AIResponse aiResponse = new AIResponse(
            message.username(),
            response
        );

        return aiResponse;
    }
}
