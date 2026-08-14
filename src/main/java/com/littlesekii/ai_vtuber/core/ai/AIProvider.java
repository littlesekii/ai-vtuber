package com.littlesekii.ai_vtuber.core.ai;

public interface AIProvider {
    String generateResponse(String context, String username, String message);
}
