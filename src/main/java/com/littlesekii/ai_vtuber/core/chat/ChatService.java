package com.littlesekii.ai_vtuber.core.chat;

public interface ChatService {
    ChatMessage listen() throws InterruptedException;
}
