package com.littlesekii.ai_vtuber.core.ai;

import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.core.interaction.InteractionType;
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

    public AIResponse process(InteractionEvent interaction) {
        System.out.println("[AI] Generating response...");
        String response = "";

        if (interaction.type() == InteractionType.MESSAGE) {
            response = provider.generateMessageResponse(
                personalityService.getPersonality(),
                interaction.username(),
                interaction.body()
            );
        } else {
            response = provider.generateEventResponse(
                personalityService.getPersonality(),
                interaction.username(),
                interaction.body()
            );
        }
        System.out.println("[AI] Generated response: " + response);

        AIResponse aiResponse = new AIResponse(
            interaction.username(),
            response
        );

        return aiResponse;
    }
}
