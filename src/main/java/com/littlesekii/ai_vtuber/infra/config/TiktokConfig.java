package com.littlesekii.ai_vtuber.infra.config;

public record TiktokConfig(
    String streamerUsername,
    double greetingChance,
    double followThanksChance,
    int restrictionTimeoutSeconds,
    int followPriorityAmount,
    int followPriorityDurationMinutes,
    int giftPriorityMultiplier,
    int giftPriorityDurationMinutes,
    double likePriorityMultiplier,
    int likePriorityDurationMinutes,
    int giftRankingTopN,
    int likeRankingTopN,
    int reconnectDelaySeconds
) {
    public static TiktokConfig from(AppConfig config) {
        return new TiktokConfig(
            config.getString    ("app.tiktok.streamer-username", "ayamelives"),
            config.getDouble    ("app.tiktok.greeting-chance", 0.3),
            config.getDouble    ("app.tiktok.follow-thanks-chance", 0.9),
            config.getInt       ("app.tiktok.restriction-timeout-seconds", 5),
            config.getInt       ("app.tiktok.follow-priority-amount", 1000),
            config.getInt       ("app.tiktok.follow-priority-duration-minutes", 2),
            config.getInt       ("app.tiktok.gift-priority-multiplier", 500),
            config.getInt       ("app.tiktok.gift-priority-duration-minutes", 3),
            config.getDouble    ("app.tiktok.like-priority-multiplier", 0.5),
            config.getInt       ("app.tiktok.like-priority-duration-minutes", 1),
            config.getInt       ("app.tiktok.gift-ranking-top-n", 5),
            config.getInt       ("app.tiktok.like-ranking-top-n", 5),
            config.getInt       ("app.tiktok.reconnect-delay-seconds", 10)
        );
    }
}
