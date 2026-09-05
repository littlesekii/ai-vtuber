package com.littlesekii.ai_vtuber.domain.interaction;

public record InteractionEvent (
    String username,
    String body,
    String profileUrl,
    InteractionType type,
    int amount,
    int priority,
    long createdAt
) implements Comparable<InteractionEvent> {

    public InteractionEvent(
        String username,
        String body,
        String profileUrl,
        InteractionType interactionType,
        int amount,
        int priority
    ) {
        this(
            username, 
            body,
            profileUrl,
            interactionType,
            amount,
            priority, 
            System.currentTimeMillis()
        );
    }

    public InteractionEvent(
        String username,
        String body,
        String profileUrl,
        int priority
    ) {
        this(
            username, 
            body,
            profileUrl,
            InteractionType.MESSAGE,
            0,
            priority, 
            System.currentTimeMillis()
        );
    }

    public InteractionEvent(
        String username,
        String body
    ) {
        this(
            username, 
            body,
            "",
            InteractionType.MESSAGE,
            0,
            0, 
            System.currentTimeMillis()
        );
    }

    @Override
    public int compareTo(InteractionEvent other) {

        int priorityComparison = Integer.compare(
            other.priority,
            this.priority
        );

        if (priorityComparison != 0) {
            return priorityComparison;
        }

        return Long.compare(
            this.createdAt,
            other.createdAt
        );
    }
}
