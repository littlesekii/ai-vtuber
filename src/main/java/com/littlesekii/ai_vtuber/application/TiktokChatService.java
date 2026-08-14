package com.littlesekii.ai_vtuber.application;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import com.littlesekii.ai_vtuber.core.chat.ChatMessage;
import com.littlesekii.ai_vtuber.core.chat.ChatService;

import io.github.jwdeveloper.tiktok.TikTokLive;

public class TiktokChatService implements ChatService {

    private final String username;
    private final BlockingQueue<ChatMessage> incomingMessages;

    public TiktokChatService(String username) {
        this.username = username;
        this.incomingMessages = new LinkedBlockingQueue<>(10);
        connect();
    }

    public void connect() {
        System.out.println("[TIKTOK] Connecting to @" + username);
        TikTokLive.newClient(username)
            .onConnected((liveClient, event) -> {
                System.out.println("[TIKTOK] Connected!");
            })
            .onComment((liveClient, event) -> {
                String viewer = event.getUser().getProfileName();
                String text = event.getText();
                incomingMessages.offer(new ChatMessage(viewer, text));
                System.out.println("[TIKTOK] " + viewer + ": " + text);
            })
            .onGift((liveClient, event) -> {
                String viewer = event.getUser().getProfileName();
                incomingMessages.offer(new ChatMessage(viewer, "enviou um presente! (nesse caso você deve agradecer)"));
            })
            .onFollow((liveClient, event) -> {
                String viewer = event.getUser().getProfileName();
                incomingMessages.offer(new ChatMessage(viewer, "segui você! (nesse caso você deve agradecer)"));
            })
            .onJoin((liveClient, event) -> {
                // Gera um número entre 0.0 e 1.0 (ex: 0.11 é 11%)
                if (ThreadLocalRandom.current().nextDouble() < 0.11) {
                    String viewer = event.getUser().getProfileName();
                    incomingMessages.offer(new ChatMessage(viewer, "entrou na live! (nesse caso você só deve dizer olá, " + viewer + ")"));
                    System.out.println("[TIKTOK] Sorteado para saudar: " + viewer);
                }
            })
            .onError((liveClient, event) -> {
                System.err.println(
                    "[TIKTOK] Error: " + event.getException().getMessage()
                );
            })
            .buildAndConnect();
    }

    @Override
    public ChatMessage listen() throws InterruptedException {
        return incomingMessages.take();
    }
}