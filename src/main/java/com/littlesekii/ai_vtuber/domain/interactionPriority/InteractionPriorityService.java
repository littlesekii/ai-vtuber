package com.littlesekii.ai_vtuber.domain.interactionPriority;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InteractionPriorityService {
    
    private final Map<String, List<InteractionPriority>> priorityMap;

    public InteractionPriorityService() {
        priorityMap = new ConcurrentHashMap<>();
    }

    public void addPriority(
        String identifier,
        int amount,
        Duration duration
    ) {
        long expiresAt = System.currentTimeMillis() + duration.toMillis();
        InteractionPriority newPriority = new InteractionPriority(amount, expiresAt);

        priorityMap.computeIfAbsent(
            identifier, 
            priorities -> new CopyOnWriteArrayList<>()
        ).add(newPriority);
    }

    public int getPriority(String username) {
        List<InteractionPriority> priorities = priorityMap.get(username);

        if (priorities == null) {
            return 0;
        }

        priorities.removeIf(priority -> !priority.isActive());

        int amount = priorities.stream()
            .mapToInt(InteractionPriority::amount)
            .sum();

        if (priorities.isEmpty()) {
            priorityMap.remove(username);
        }

        return amount;
    } 
}
