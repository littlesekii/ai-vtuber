package com.littlesekii.ai_vtuber.domain.interactionModeration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InteractionModerationService {
    
    public enum Result {
        ALLOW,
        BLOCK,
        BLOCKED
    }

    private final Map<String, String> userLastMessage;
    private final Map<String, Long> restrictedUsers;
    private final Duration restrictionTimeout;

    public InteractionModerationService(Duration restrictionTimeout) {
        this.userLastMessage = new ConcurrentHashMap<>();
        this.restrictedUsers = new ConcurrentHashMap<>();
        this.restrictionTimeout = restrictionTimeout;
    }

    public Result moderate(String username, String message) {
        if (restrictedUsers.containsKey(username)) {
            long restrictionExpiration = restrictedUsers.get(username);
            if (System.currentTimeMillis() < restrictionExpiration) {
                return Result.BLOCKED;
            }
            restrictedUsers.remove(username);
        }

        String lastMessage = userLastMessage.get(username);

        // Repeated last message
        if (lastMessage != null && lastMessage.equalsIgnoreCase(message)) {
            restrictedUsers.put(
                username, 
                System.currentTimeMillis() + restrictionTimeout
                    .toMillis()
            );
            return Result.BLOCK;
        }
        userLastMessage.put(username, message);
        return Result.ALLOW;
    }
}
