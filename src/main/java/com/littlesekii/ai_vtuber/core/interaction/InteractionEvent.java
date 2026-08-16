package com.littlesekii.ai_vtuber.core.interaction;

public record InteractionEvent (
    String username,
    String body,
    String profileUrl,
    InteractionType type,
    int priority,
    long createdAt
) implements Comparable<InteractionEvent> {

    public InteractionEvent(
        String username,
        String body,
        String profileUrl,
        InteractionType interactionType,
        int priority
    ) {
        this(
            username, 
            body,
            profileUrl,
            interactionType,
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
            priority, 
            System.currentTimeMillis()
        );
    }

    public InteractionEvent(
        String username,
        String body,
        InteractionType interactionType
    ) {
        this(
            username, 
            body,
            "",
            interactionType,
            0, 
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
