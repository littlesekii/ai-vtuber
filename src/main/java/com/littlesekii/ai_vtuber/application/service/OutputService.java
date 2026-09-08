package com.littlesekii.ai_vtuber.application.service;

import com.littlesekii.ai_vtuber.application.model.InteractionData;
import com.littlesekii.ai_vtuber.application.port.out.AudioPlayerProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.AvatarProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.OverlayProviderPort;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionType;

public class OutputService {
    
    private final AvatarProviderPort avatarProviderPort;
    private final OverlayProviderPort overlayProviderPort;
    private final AudioPlayerProviderPort audioProviderPort;


    public OutputService(
        AvatarProviderPort avatarProviderPort, 
        OverlayProviderPort overlayProviderPort,
        AudioPlayerProviderPort audioProviderPort
    ) {
        this.avatarProviderPort = avatarProviderPort;
        this.overlayProviderPort = overlayProviderPort;
        this.audioProviderPort = audioProviderPort;
    }

    public void process(InteractionData interactionData) {        
        InteractionEvent interaction = interactionData.getInteractionEvent();
        switch (interaction.type()) {
            case InteractionType.FOLLOW:         
                avatarProviderPort.follow();
                break;
            case InteractionType.GIFT:   
                avatarProviderPort.gift(interaction.amount());
                break;            
            default:                
                break;
        }
        overlayProviderPort.showMessage(
            interaction.username(), 
            interaction.body(), 
            interaction.profileUrl(), 
            interaction.priority()
        );
        audioProviderPort.play(interactionData.getTTSAudio());        
    }
}
