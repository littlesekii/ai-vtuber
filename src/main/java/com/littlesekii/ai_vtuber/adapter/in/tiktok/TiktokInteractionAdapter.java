package com.littlesekii.ai_vtuber.adapter.in.tiktok;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.littlesekii.ai_vtuber.application.port.in.InteractionProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.OverlayProviderPort;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionType;
import com.littlesekii.ai_vtuber.domain.interactionModeration.InteractionModerationService;
import com.littlesekii.ai_vtuber.domain.interactionModeration.InteractionModerationService.Result;
import com.littlesekii.ai_vtuber.domain.interactionPriority.InteractionPriorityService;
import com.littlesekii.ai_vtuber.domain.interactionRanking.InteractionRankingService;
import com.littlesekii.ai_vtuber.infra.config.TiktokConfig;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.websocket.LiveClientStopType;

public class TiktokInteractionAdapter implements InteractionProviderPort {

    private final TiktokConfig config;
    private final OverlayProviderPort overlayProviderPort;
    
    private final BlockingQueue<InteractionEvent> incomingInteractions;
    
    private final InteractionPriorityService priorityService;
    private final InteractionModerationService moderationService;
    private final InteractionRankingService likeRankingService;
    private final InteractionRankingService giftRankingService;

    private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "tiktok-reconnect");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicReference<LiveClient> liveClientRef = new AtomicReference<>(null);

    public TiktokInteractionAdapter(
        TiktokConfig config,
        OverlayProviderPort overlayProviderPort
    ) {
        this.config = config;
        this.overlayProviderPort = overlayProviderPort;
        
        this.priorityService = new InteractionPriorityService();
        this.moderationService = new InteractionModerationService(
            Duration.ofSeconds(config.restrictionTimeoutSeconds())
        );
        this.likeRankingService = new InteractionRankingService();
        this.giftRankingService = new InteractionRankingService();

        this.incomingInteractions = new LinkedBlockingQueue<>();

        connect();
    }

    public void connect() {
        if (!running.get()) {
            return;
        }
    
        System.out.println("[TIKTOK] Connecting to @" + config.streamerUsername());
        try {
            CompletableFuture<LiveClient> future = TikTokLive.newClient(config.streamerUsername())
            .configure(settings -> {
                settings.setRetryOnConnectionFailure(false);
            })
            .onConnected((liveClient, event) -> {
                System.out.println("[TIKTOK] Connected!");
                liveClientRef.set(liveClient);
            })
            .onJoin((liveClient, event) -> {
                if (ThreadLocalRandom.current().nextDouble() < config.greetingChance()) {
                    String viewerName = event.getUser().getProfileName();
                    String viewerProfileUrl = event.getUser().getPicture().getLink();
                    incomingInteractions.offer(new InteractionEvent(viewerName, "entrou na live!", viewerProfileUrl, 0));
                    System.out.println("[TIKTOK] Sorteado para saudar: " + viewerName);
                }
            })
            .onComment((liveClient, event) -> {
                String viewerUsername = event.getUser().getName();
                String viewerName = event.getUser().getProfileName();
                String viewerMessage = event.getText();
                String viewerProfileUrl = event.getUser().getPicture().getLink();
                int viewerPriority = priorityService.getPriority(viewerUsername);

                Result moderationResult = moderationService.moderate(viewerUsername, viewerMessage);
                if (moderationResult != Result.ALLOW) {
                    if (moderationResult == Result.BLOCK) {
                        incomingInteractions.removeIf(
                            interaction -> interaction.username().equals(viewerUsername)
                        );    
                    }
                    return;
                }

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
                    config.followPriorityAmount(), 
                    Duration.ofMinutes(config.followPriorityDurationMinutes())
                );

                if (ThreadLocalRandom.current().nextDouble() <= config.followThanksChance()) {
                    int viewerPriority = priorityService.getPriority(viewerUsername);

                    incomingInteractions.offer(new InteractionEvent(
                        viewerName, 
                        "Seguiu você.",
                        viewerProfileUrl,
                        InteractionType.FOLLOW,
                        0,
                        viewerPriority
                    ));
                }
            })
            .onGiftCombo((liveClient, event) -> {
                if (event.getComboState() == GiftComboStateType.Finished) {
                    String viewerUsername = event.getUser().getName();
                    String viewerName = event.getUser().getProfileName();
                    String viewerProfileUrl = event.getUser().getPicture().getLink();
                
                    String giftName = event.getGift().getName();
                    int giftValue = event.getGift().getDiamondCost();
                    int giftAmount = event.getCombo();
                    String giftImageUrl = event.getGift().getPicture().getLink();
                    
                    priorityService.addPriority(
                        viewerUsername, 
                        config.giftPriorityMultiplier() * giftValue * giftAmount, 
                        Duration.ofMinutes(config.giftPriorityDurationMinutes())
                    );

                    overlayProviderPort.showGift(
                        viewerName, 
                        viewerProfileUrl,
                        giftName,
                        giftImageUrl,
                        giftAmount
                    );

                    giftRankingService.add(
                        viewerUsername, 
                        viewerName, 
                        viewerProfileUrl, 
                        giftValue * giftAmount
                    );
                    overlayProviderPort.updateGiftRanking(
                        giftRankingService.getTopRanking(config.giftRankingTopN())
                    );


                    int viewerPriority = priorityService.getPriority(viewerUsername);
                    incomingInteractions.offer(new InteractionEvent(
                        viewerName, 
                        "Enviou " + giftAmount + 
                            " " + giftName + 
                            ". Valor total: " + giftValue * giftAmount + ".",
                        viewerProfileUrl,
                        InteractionType.GIFT,
                        giftAmount * giftValue,
                        viewerPriority
                    ));
                }
            })
            .onLike((client, event) -> {
                String viewerUsername = event.getUser().getName();
                String viewerName = event.getUser().getProfileName();
                String viewerImageUrl = event.getUser().getPicture().getLink();
                Integer likes = event.getLikes();

                int priority = (int) Math.floor(likes * config.likePriorityMultiplier());

                priorityService.addPriority(
                    viewerUsername,
                    priority,
                    Duration.ofMinutes(config.likePriorityDurationMinutes())
                );

                likeRankingService.add(
                    viewerUsername, 
                    viewerName, 
                    viewerImageUrl, 
                    likes
                );
                overlayProviderPort.updateLikeRanking(
                    likeRankingService.getTopRanking(config.likeRankingTopN())
                );

            })    
            .onDisconnected((liveClient, event) -> {
                System.out.println("[TIKTOK] Disconnected. Reconnecting in " + config.reconnectDelaySeconds() + "s...");
                liveClientRef.set(null);
                scheduleReconnect();
            })
            .onError((liveClient, event) -> {
                System.err.println(
                    "[TIKTOK] Error: " + event.getException().getMessage()
                );
            })
            .buildAndConnectAsync();

            future.whenComplete((client, ex) -> {
                if (ex != null) {
                    liveClientRef.set(null);
                    scheduleReconnect();
                } else {
                    liveClientRef.set(client);
                }
            });

        } catch (Exception e) {
            System.err.println("[TIKTOK] Could not build client: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (!running.get()) {
            return;
        }
        if (!reconnectScheduled.compareAndSet(false, true)) {
            return;
        }
        reconnectScheduler.schedule(
            () -> {
                reconnectScheduled.set(false);
                connect();
            },
            config.reconnectDelaySeconds(),
            TimeUnit.SECONDS
        );
    }

    public void stop() {
        running.set(false);
        LiveClient client = liveClientRef.get();
        if (client != null) {
            try {
                client.disconnect(LiveClientStopType.DISCONNECT);
            } catch (Exception ignored) {}
        }
        reconnectScheduler.shutdownNow();
    }

    @Override
    public InteractionEvent listen() throws InterruptedException {
        return incomingInteractions.take();
    }
}