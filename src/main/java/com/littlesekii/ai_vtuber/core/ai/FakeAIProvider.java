package com.littlesekii.ai_vtuber.core.ai;

public class FakeAIProvider implements AIProvider {

    @Override
    public String generateResponse(String context, String username, String message) {
        return "Oii " + username + "! Você disse: " + message;
    }
}
