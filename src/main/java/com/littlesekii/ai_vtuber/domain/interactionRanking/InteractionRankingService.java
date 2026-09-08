package com.littlesekii.ai_vtuber.domain.interactionRanking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InteractionRankingService {

    private static final String VALUE_KEY = "amount";
    
    private final Map<String, Map<String, Object>> ranking;

    public InteractionRankingService() {
        this.ranking = new ConcurrentHashMap<>();
    }

    public void add(
        String username,
        String name,
        String profileImageUrl,
        int value
    ) {
        Map<String, Object> userStats = ranking.computeIfAbsent(
            username,
            key -> new HashMap<>()
        );
        userStats.put("name", name);
        userStats.put("profileImageUrl", profileImageUrl);

        int currentValue = (int) userStats.getOrDefault(VALUE_KEY, 0);
        userStats.put(VALUE_KEY, currentValue + value);
    }
    
    public List<Map<String, Object>> getTopRanking(int topN) {
        return ranking.values()
            .stream()
            .sorted(
                (a, b) -> Integer.compare(
                    (int) b.get(VALUE_KEY),
                    (int) a.get(VALUE_KEY) 
                )
            )
            .limit(topN)
            .toList();
    }

}
