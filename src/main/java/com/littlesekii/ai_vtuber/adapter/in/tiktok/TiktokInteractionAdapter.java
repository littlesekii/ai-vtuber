package com.littlesekii.ai_vtuber.adapter.in.tiktok;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
import com.littlesekii.ai_vtuber.domain.priority.PriorityService;
import com.littlesekii.ai_vtuber.infra.config.TiktokConfig;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.websocket.LiveClientStopType;

public class TiktokInteractionAdapter implements InteractionProviderPort {

    private final TiktokConfig config;
    private final PriorityService priorityService;
    private final OverlayProviderPort overlayProviderPort;

    private final BlockingQueue<InteractionEvent> incomingInteractions;

    private final Map<String, String> userLastMessage;
    private final Map<String, Long> restrictedUsers;
    private final Map<String, Map<String, Object>> likeRanking;
    private final Map<String, Map<String, Object>> giftRanking;

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
        PriorityService priorityService,
        OverlayProviderPort overlayProviderPort
    ) {
        this.config = config;
        this.priorityService = priorityService;
        this.overlayProviderPort = overlayProviderPort;

        this.incomingInteractions = new LinkedBlockingQueue<>();
        this.userLastMessage = new ConcurrentHashMap<>();
        this.restrictedUsers = new ConcurrentHashMap<>();
        this.likeRanking = new ConcurrentHashMap<>();
        this.giftRanking = new ConcurrentHashMap<>();

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

                if (restrictedUsers.containsKey(viewerUsername)) {
                    long restrictionExpiration = restrictedUsers.get(viewerUsername);
                    if (System.currentTimeMillis() < restrictionExpiration) {
                        return;
                    }
                    restrictedUsers.remove(viewerUsername);
                }

                String lastMessage = userLastMessage.get(viewerUsername);

                // Repeated last message
                if (lastMessage != null && lastMessage.equalsIgnoreCase(viewerMessage)) {
                    restrictedUsers.put(
                        viewerUsername, 
                        System.currentTimeMillis() + Duration.ofSeconds(config.restrictionTimeoutSeconds())
                            .toMillis()
                    );
                    incomingInteractions.removeIf(
                        (interaction) -> interaction.username().equals(viewerUsername)
                    );
                    return;
                }

                incomingInteractions.offer(new InteractionEvent(
                    viewerName, 
                    viewerMessage, 
                    viewerProfileUrl,
                    viewerPriority
                ));

                userLastMessage.put(viewerUsername, viewerMessage);

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
                
                    String viewerGiftName = event.getGift().getName();
                    int viewerGiftValue = event.getGift().getDiamondCost();
                    int viewerGiftAmount = event.getCombo();
                    String viewerGiftImageUrl = event.getGift().getPicture().getLink();
                    
                    priorityService.addPriority(
                        viewerUsername, 
                        config.giftPriorityMultiplier() * viewerGiftAmount * viewerGiftValue, 
                        Duration.ofMinutes(config.giftPriorityDurationMinutes())
                    );

                    overlayProviderPort.showGift(
                        viewerName, 
                        viewerProfileUrl,
                        viewerGiftName,
                        viewerGiftImageUrl,
                        viewerGiftAmount
                    );


                    Map<String, Object> userGifts = giftRanking.computeIfAbsent(
                        viewerUsername, 
                        key -> new HashMap<>()
                    );
                    userGifts.put("name", viewerName);
                    userGifts.put("profileImageUrl", viewerProfileUrl);

                    int currentCoins = (int) userGifts.getOrDefault(
                        "coins",
                        0
                    );
                    userGifts.put("coins", currentCoins + viewerGiftAmount * viewerGiftValue);

                    List<Map<String, Object>> top5 = giftRanking.values()
                        .stream()
                        .sorted(
                            (a, b) -> Integer.compare(
                                (int) b.get("coins"),
                                (int) a.get("coins")
                            )
                        )
                    .limit(config.giftRankingTopN())
                    .toList();

                    overlayProviderPort.updateGiftRanking(top5);


                    int viewerPriority = priorityService.getPriority(viewerUsername);
                    incomingInteractions.offer(new InteractionEvent(
                        viewerName, 
                        "Enviou " + viewerGiftAmount + 
                            " " + viewerGiftName + 
                            ". Valor total: " + viewerGiftValue * viewerGiftAmount + ".",
                        viewerProfileUrl,
                        InteractionType.GIFT,
                        viewerGiftAmount * viewerGiftValue,
                        viewerPriority
                    ));
                }
            })
            .onLike((client, event) -> {
                String username = event.getUser().getName();
                String name = event.getUser().getProfileName();
                String profileUrl = event.getUser().getPicture().getLink();
                Integer likes = event.getLikes();

                int priority = (int) Math.floor(likes * config.likePriorityMultiplier());

                priorityService.addPriority(
                    username,
                    priority,
                    Duration.ofMinutes(config.likePriorityDurationMinutes())
                );

                Map<String, Object> userLikes = likeRanking.computeIfAbsent(
                    username, 
                    key -> new HashMap<>()
                );
                userLikes.put("name", name);
                userLikes.put("profileImageUrl", profileUrl);

                int currentLikes = (int) userLikes.getOrDefault(
                    "likes",
                    0
                );
                userLikes.put("likes", currentLikes + likes);

                 List<Map<String, Object>> top5 = likeRanking.values()
                    .stream()
                    .sorted(
                        (a, b) -> Integer.compare(
                            (int) b.get("likes"),
                            (int) a.get("likes")
                        )
                    )
                .limit(config.likeRankingTopN())
                .toList();

                overlayProviderPort.updateLikeRanking(top5);
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