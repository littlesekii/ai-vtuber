package com.littlesekii.ai_vtuber.core.priority;

public record Priority (
    int amount,
    long expiresAt
) {
    public boolean isActive() {
        return System.currentTimeMillis() < expiresAt;
    }
}
