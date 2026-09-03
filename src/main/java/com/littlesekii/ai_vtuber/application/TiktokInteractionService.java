package com.littlesekii.ai_vtuber.application;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.core.interaction.InteractionService;
import com.littlesekii.ai_vtuber.core.interaction.InteractionType;
import com.littlesekii.ai_vtuber.core.priority.PriorityService;
import com.littlesekii.ai_vtuber.overlay.OverlayController;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;

public class TiktokInteractionService implements InteractionService {


    private final Double CHANCE_GREETING = 10.0 / 100;
    private final Double CHANCE_THANKS_FOLLOW = 57.0 / 100;

    private final Integer RESTRICTION_TIMEOUT = 20;

    private final String streamerUsername;
    private final BlockingQueue<InteractionEvent> incomingInteractions;

    private final Map<String, String> userLastMessage;
    private final Map<String, Long> restrictedUsers;
    private final Map<String, Map<String, Object>> likeRanking;
    private final Map<String, Map<String, Object>> giftRanking;

    private final PriorityService priorityService;

    private final OverlayController overlayController;


    public TiktokInteractionService(
        String streamerUsername, 
        PriorityService priorityService,
        OverlayController overlayController
    ) {
        this.streamerUsername = streamerUsername;
        this.incomingInteractions = new LinkedBlockingQueue<>();
        this.priorityService = priorityService;
        this.overlayController = overlayController;
        this.userLastMessage = new HashMap<>();
        this.restrictedUsers = new HashMap<>();
        this.likeRanking = new HashMap<>();
        this.giftRanking = new HashMap<>();

        connect();
    }

    public void connect() {
        System.out.println("[TIKTOK] Connecting to @" + streamerUsername);
        TikTokLive.newClient(streamerUsername)
            .onConnected((liveClient, event) -> {
                System.out.println("[TIKTOK] Connected!");
            })
            .onJoin((liveClient, event) -> {
                if (ThreadLocalRandom.current().nextDouble() < CHANCE_GREETING) {
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
                        System.currentTimeMillis() + Duration.ofSeconds(RESTRICTION_TIMEOUT)
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
                    1000, 
                    Duration.ofMinutes(2)
                );

                if (ThreadLocalRandom.current().nextDouble() <= CHANCE_THANKS_FOLLOW) {
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
                        500 * viewerGiftAmount * viewerGiftValue, 
                        Duration.ofMinutes(3)
                    );

                    overlayController.showGift(
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
                    .limit(5)
                    .toList();

                    overlayController.updateGiftRanking(top5);


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

                int priority = (int) Math.floor(likes * 0.5);

                priorityService.addPriority(
                    username,
                    priority,
                    Duration.ofMinutes(1)
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
                .limit(5)
                .toList();

                overlayController.updateLikeRanking(top5);
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