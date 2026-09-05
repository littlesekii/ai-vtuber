package com.littlesekii.ai_vtuber.application.port.out;

import java.util.List;
import java.util.Map;

public interface OverlayProviderPort {
    void showMessage(
        String username,
        String message,
        String profileImageUrl,
        int priority
    );

    void showGift(
        String username,
        String profileImageUrl,
        String giftName,
        String giftImageUrl,
        int amount
    );

    void updateLikeRanking(List<Map<String, Object>> ranking);
    void updateGiftRanking(List<Map<String, Object>> ranking);

    void close();
}
