package com.littlesekii.ai_vtuber.core.interaction;

import java.nio.file.Path;

public class InteractionData implements Comparable<InteractionData> {

    private InteractionEvent interactionEvent;
    private String aiResponse;
    private Path ttsAudio;

    private boolean interactionSlot;

    public InteractionEvent getInteractionEvent() {
        return interactionEvent;
    }
    public void setInteractionEvent(InteractionEvent interactionEvent) {
        this.interactionEvent = interactionEvent;
    }

    public String getAIResponse() {
        return aiResponse;
    }
    public void setAIResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public Path getTTSAudio() {
        return ttsAudio;
    }
    public void setTTSAudio(Path ttsAudio) {
        this.ttsAudio = ttsAudio;
    }

    public boolean getInteractionSlot() {
        return interactionSlot;
    }
    public void setInteractionSlot(boolean interactionSlot) {
        this.interactionSlot = interactionSlot;
    }


    @Override
    public int compareTo(InteractionData other) {
        return this.interactionEvent.compareTo(
            other.interactionEvent
        );
    }
}
