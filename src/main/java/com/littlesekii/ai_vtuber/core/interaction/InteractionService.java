package com.littlesekii.ai_vtuber.core.interaction;

public interface InteractionService {
    InteractionEvent listen() throws InterruptedException;
}
