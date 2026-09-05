package com.littlesekii.ai_vtuber.application.port.in;

import com.littlesekii.ai_vtuber.domain.interaction.InteractionEvent;

public interface InteractionProviderPort {
    InteractionEvent listen() throws InterruptedException;
}
