package com.littlesekii.ai_vtuber.core.ai;

public interface AIProvider {
    String generateMessageResponse(String context, String username, String message);
    String generateEventResponse(String context, String username, String event);
}
