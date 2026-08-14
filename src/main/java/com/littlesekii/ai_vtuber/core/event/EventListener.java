package com.littlesekii.ai_vtuber.core.event;

@FunctionalInterface
public interface EventListener<T> {
    void onEvent(T event);
}
