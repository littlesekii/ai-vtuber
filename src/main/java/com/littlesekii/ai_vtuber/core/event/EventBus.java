package com.littlesekii.ai_vtuber.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>())
            .add(listener);
    }

    public <T> void publish(T event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());

        if (eventListeners == null) {
            return;
        }

        for (EventListener<?> listener : eventListeners) {
            notifyListener(listener, event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void notifyListener(EventListener<?> listener, T event) {
        ((EventListener<T>) listener).onEvent(event);
    }

    
}
