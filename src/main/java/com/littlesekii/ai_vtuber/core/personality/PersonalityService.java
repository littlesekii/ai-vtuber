package com.littlesekii.ai_vtuber.core.personality;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PersonalityService {

    private final String personality;

    public PersonalityService(Path personalityFile) {

        try {
            this.personality = Files.readString(personalityFile);

        } catch (IOException e) {
            throw new RuntimeException(
                "Não foi possível carregar a personalidade: " + 
                personalityFile,
                e
            );
        }
    }

    public String getPersonality() {
        return personality;
    }
}