package com.littlesekii.ai_vtuber.application.service;

import com.littlesekii.ai_vtuber.application.port.out.AIProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.PersonalityProviderPort;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionType;
import com.littlesekii.ai_vtuber.domain.personality.Personality;

public class AIService {
    private final AIProviderPort provider;
    private final Personality personality;

    public AIService(
        AIProviderPort provider,
        PersonalityProviderPort personalityProviderPort
    ) {
        this.provider = provider;
        this.personality = personalityProviderPort.load();
    }

    public String generateResponse(InteractionEvent interaction) {
        System.out.println("[AI] Generating response...");

        String context = personality.text();
        String response = "";

        if (interaction.type() == InteractionType.MESSAGE) {
            response = provider.generateMessageResponse(
                context,
                interaction.username(),
                interaction.body()
            );
        } else {
            response = provider.generateEventResponse(
                context,
                interaction.username(),
                interaction.body()
            );
        }
        System.out.println("[AI] Generated response: " + response);
        return response;
    }
}
