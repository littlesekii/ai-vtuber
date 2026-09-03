package com.littlesekii.ai_vtuber.output;

import com.littlesekii.ai_vtuber.audio.AudioPlayerService;
import com.littlesekii.ai_vtuber.avatar.VNyanAvatarController;
import com.littlesekii.ai_vtuber.core.interaction.InteractionData;
import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.core.interaction.InteractionType;
import com.littlesekii.ai_vtuber.overlay.OverlayController;

public class OutputService {
    
    private VNyanAvatarController vnyanAvatarController;
    private OverlayController overlayController;
    private AudioPlayerService audioPlayerService;

    public OutputService(VNyanAvatarController vnyanAvatarController, OverlayController overlayController) {
        this.audioPlayerService = new AudioPlayerService(vnyanAvatarController);
        this.vnyanAvatarController = vnyanAvatarController;
        this.overlayController = overlayController;
    }

    public void process(InteractionData interactionData) {

        InteractionEvent interaction = interactionData.getInteractionEvent();
        if (interaction.type() == InteractionType.GIFT) {
            overlayController.showMessage(interaction.username(), interaction.body(), interaction.profileUrl(), interaction.priority());            
            vnyanAvatarController.gift(interaction.amount());
        }
        else if (interaction.type() == InteractionType.FOLLOW) {
            overlayController.showMessage(interaction.username(), "Acabou de seguir!", interaction.profileUrl(), interaction.priority());
            vnyanAvatarController.follow();
        } else {
            overlayController.showMessage(interaction.username(), interaction.body(), interaction.profileUrl(), interaction.priority());
        }

        audioPlayerService.process(interactionData.getTTSAudio());
    }
}
