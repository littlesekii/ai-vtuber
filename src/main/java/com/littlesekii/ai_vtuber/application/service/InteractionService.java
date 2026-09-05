package com.littlesekii.ai_vtuber.application.service;

import com.littlesekii.ai_vtuber.application.port.in.InteractionProviderPort;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionEvent;

public class InteractionService {

    private final InteractionProviderPort provider;

    public InteractionService(InteractionProviderPort provider) {
        this.provider = provider;
    }

    public InteractionEvent listen() throws InterruptedException {
        return provider.listen();
    }
}
