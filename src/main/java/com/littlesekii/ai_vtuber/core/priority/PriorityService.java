package com.littlesekii.ai_vtuber.core.priority;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PriorityService {
    
    private final Map<String, List<Priority>> priorityMap = new ConcurrentHashMap<>();

    public void addPriority(
        String identifier,
        int amount,
        Duration duration
    ) {
        long expiresAt = System.currentTimeMillis() + duration.toMillis();
        Priority newPriority = new Priority(amount, expiresAt);

        priorityMap.computeIfAbsent(
            identifier, 
            priorities -> new CopyOnWriteArrayList<>()
        ).add(newPriority);
    }

    public int getPriority(String username) {
        List<Priority> priorities = priorityMap.get(username);

        if (priorities == null) {
            return 0;
        }

        priorities.removeIf(priority -> !priority.isActive());

        int amount = priorities.stream()
            .mapToInt(Priority::amount)
            .sum();

        if (priorities.isEmpty()) {
            priorityMap.remove(username);
        }

        return amount;
    } 
}
