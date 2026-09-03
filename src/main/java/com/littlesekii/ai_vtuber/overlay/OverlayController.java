package com.littlesekii.ai_vtuber.overlay;

import java.util.List;
import java.util.Map;

public class OverlayController {

    private final OverlayWebSocketClient client;

    public OverlayController(
        OverlayWebSocketClient client
    ) {
        this.client = client;
    }

    public void showMessage(
        String username,
        String message,
        String profileImageUrl,
        int priority
    ) {
        client.send(
            "MESSAGE",
            Map.of(
                "username", username,
                "message", message,
                "profileImageUrl", profileImageUrl,
                "priority", priority
            )
        );
    }

    public void showGift(
        String username,
        String profileImageUrl,
        String giftName,
        String giftImageUrl,
        int amount
    ) {
        client.send(
            "LAST_GIFT",
            Map.of(
                "username", username,
                "profileImageUrl", profileImageUrl,
                "giftName", giftName,
                "giftImageUrl", giftImageUrl,
                "amount", amount
            )
        );
    }

    public void updateLikeRanking(
        List<Map<String, Object>> ranking
    ) {
        client.send(
            "LIKE_RANKING",
            ranking
        );
    }

    public void updateGiftRanking(
        List<Map<String, Object>> ranking
    ) {
        client.send(
            "GIFT_RANKING",
            ranking
        );
    }

    public void close() {
        client.close();
    }
}