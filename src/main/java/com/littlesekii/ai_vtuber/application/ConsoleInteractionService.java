package com.littlesekii.ai_vtuber.application;

import java.util.Scanner;

import com.littlesekii.ai_vtuber.core.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.core.interaction.InteractionService;
import com.littlesekii.ai_vtuber.core.interaction.InteractionType;

public class ConsoleInteractionService implements InteractionService {

    private final Scanner scanner;

    public ConsoleInteractionService() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public InteractionEvent listen() {
        System.out.print("> ");

        String input = scanner.nextLine();

        return parse(input);
    }

    public InteractionEvent parse(String input) {

        int separator = input.indexOf('|');

        if (separator == -1) {
            return new InteractionEvent(
                "Viewer",
                input,
                "https://static.vecteezy.com/system/resources/thumbnails/048/496/223/small_2x/an-image-of-girl-listening-to-music-photo.jpeg",
                InteractionType.MESSAGE,
                0,
                0
            );
        }

        String username = input
            .substring(0, separator)
            .trim();

        String message = input
            .substring(separator + 1)
            .trim();

        if (username.equalsIgnoreCase("gift")) {
            return new InteractionEvent(
                username,
                message,
                "https://static.vecteezy.com/system/resources/thumbnails/048/496/223/small_2x/an-image-of-girl-listening-to-music-photo.jpeg",
                InteractionType.GIFT,
                5,
                500
            );
        }

        if (username.equalsIgnoreCase("follow")) {
            return new InteractionEvent(
                username,
                message,
                "https://static.vecteezy.com/system/resources/thumbnails/048/496/223/small_2x/an-image-of-girl-listening-to-music-photo.jpeg",
                InteractionType.FOLLOW,
                0,
                1000
            );
        }

        return new InteractionEvent(
            username,
            message,
            "https://static.vecteezy.com/system/resources/thumbnails/048/496/223/small_2x/an-image-of-girl-listening-to-music-photo.jpeg",
            InteractionType.MESSAGE,
            0,
            0
        );
    }
}