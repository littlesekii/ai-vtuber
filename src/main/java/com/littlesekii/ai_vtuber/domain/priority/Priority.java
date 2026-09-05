package com.littlesekii.ai_vtuber.domain.priority;

public record Priority (
    int amount,
    long expiresAt
) {
    public boolean isActive() {
        return System.currentTimeMillis() < expiresAt;
    }
}
