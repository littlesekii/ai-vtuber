package com.littlesekii.ai_vtuber.adapter.out.personality;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.littlesekii.ai_vtuber.application.port.out.PersonalityProviderPort;
import com.littlesekii.ai_vtuber.domain.personality.Personality;

public class FilePersonalityAdapter implements PersonalityProviderPort {

    private final Personality personality;

    public FilePersonalityAdapter(Path personalityFile) {

        try {
            String text = Files.readString(personalityFile);
            this.personality = new Personality(text);
            
        } catch (IOException e) {
            throw new RuntimeException(
                "Não foi possível carregar a personalidade: " + 
                personalityFile,
                e
            );
        }
    }

    @Override
    public Personality load() {
        return personality;
    }
}