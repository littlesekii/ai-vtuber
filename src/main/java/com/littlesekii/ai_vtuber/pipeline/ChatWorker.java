package com.littlesekii.ai_vtuber.pipeline;

import java.util.concurrent.BlockingQueue;

import com.littlesekii.ai_vtuber.core.chat.ChatMessage;
import com.littlesekii.ai_vtuber.core.chat.ChatService;

public class ChatWorker implements Runnable {

    private final BlockingQueue<ChatMessage> messageQueue;
    private final ChatService chatService;

    public ChatWorker(
        BlockingQueue<ChatMessage> messageQueue,
        ChatService chatService
    ) {
        this.messageQueue = messageQueue;
        this.chatService = chatService;
    } 

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ChatMessage message = chatService.listen();
                messageQueue.offer(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
