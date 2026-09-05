package com.littlesekii.ai_vtuber.application.port.out;

import com.littlesekii.ai_vtuber.domain.personality.Personality;

public interface PersonalityProviderPort {
    Personality load();
}
