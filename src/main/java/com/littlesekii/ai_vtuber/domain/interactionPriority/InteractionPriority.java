package com.littlesekii.ai_vtuber.domain.interactionPriority;

public record InteractionPriority (
    int amount,
    long expiresAt
) {
    public boolean isActive() {
        return System.currentTimeMillis() < expiresAt;
    }
}
