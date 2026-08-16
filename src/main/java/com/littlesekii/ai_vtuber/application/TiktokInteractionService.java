package com.littlesekii.ai_vtuber.application;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.core.interaction.InteractionService;
import com.littlesekii.ai_vtuber.core.interaction.InteractionType;
import com.littlesekii.ai_vtuber.core.priority.PriorityService;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;

public class TiktokInteractionService implements InteractionService {

    private final String streamerUsername;
    private final BlockingQueue<InteractionEvent> incomingInteractions;

    private final PriorityService priorityService;

    public TiktokInteractionService(
        String streamerUsername, 
        PriorityService priorityService
    ) {
        this.streamerUsername = streamerUsername;
        this.incomingInteractions = new LinkedBlockingQueue<>();
        this.priorityService = priorityService;
        connect();
    }

    public void connect() {
        System.out.println("[TIKTOK] Connecting to @" + streamerUsername);
        TikTokLive.newClient(streamerUsername)
            .onConnected((liveClient, event) -> {
                System.out.println("[TIKTOK] Connected!");
            })
            .onJoin((liveClient, event) -> {
                if (ThreadLocalRandom.current().nextDouble() < 0.11) {
                    String viewerName = event.getUser().getProfileName();
                    incomingInteractions.offer(new InteractionEvent(viewerName, "entrou na live!"));
                    System.out.println("[TIKTOK] Sorteado para saudar: " + viewerName);
                }
            })
            .onComment((liveClient, event) -> {
                String viewerUsername = event.getUser().getName();
                String viewerName = event.getUser().getProfileName();
                String viewerMessage = event.getText();
                String viewerProfileUrl = event.getUser().getPicture().getLink();
                int viewerPriority = priorityService.getPriority(viewerUsername);

                incomingInteractions.offer(new InteractionEvent(
                    viewerName, 
                    viewerMessage, 
                    viewerProfileUrl,
                    viewerPriority
                ));
                System.out.println("[TIKTOK] " + viewerName + ": " + viewerMessage);
            })
            .onFollow((liveClient, event) -> {
                String viewerUsername = event.getUser().getName();
                String viewerName = event.getUser().getProfileName();
                String viewerProfileUrl = event.getUser().getPicture().getLink();

                priorityService.addPriority(
                    viewerUsername, 
                    1000, 
                    Duration.ofMinutes(2)
                );

                int viewerPriority = priorityService.getPriority(viewerUsername);

                incomingInteractions.offer(new InteractionEvent(
                    viewerName, 
                    "Seguiu você.",
                    viewerProfileUrl,
                    InteractionType.FOLLOW,
                    viewerPriority
                ));
            })
            .onGiftCombo((liveClient, event) -> {
                if (event.getComboState() == GiftComboStateType.Finished) {
                    String viewerUsername = event.getUser().getName();
                    String viewerName = event.getUser().getProfileName();
                    String viewerProfileUrl = event.getUser().getPicture().getLink();
                
                    String viewerGiftName = event.getGift().getName();
                    int viewerGiftValue = event.getGift().getDiamondCost();
                    int viewerGiftAmount = event.getCombo();
                    
                    priorityService.addPriority(
                        viewerUsername, 
                        500 * viewerGiftAmount * viewerGiftValue, 
                        Duration.ofMinutes(3)
                    );

                    int viewerPriority = priorityService.getPriority(viewerUsername);
                    incomingInteractions.offer(new InteractionEvent(
                        viewerName, 
                        "Enviou " + viewerGiftAmount + 
                            " " + viewerGiftName + 
                            ". Valor total: " + viewerGiftValue * viewerGiftAmount + ".",
                        viewerProfileUrl,
                        InteractionType.GIFT,
                        viewerPriority
                    ));
                }
            })
            .onLike((client, event) -> {
                String username = event.getUser().getName();
                int priority = (int) Math.floor(event.getLikes() * 0.5);

                priorityService.addPriority(
                    username,
                    priority,
                    Duration.ofMinutes(1)
                );
            })    
            .onError((liveClient, event) -> {
                System.err.println(
                    "[TIKTOK] Error: " + event.getException().getMessage()
                );
            })
            .buildAndConnect();
    }

    @Override
    public InteractionEvent listen() throws InterruptedException {
        return incomingInteractions.take();
    }
}