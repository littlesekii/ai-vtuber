package com.littlesekii.ai_vtuber.adapter.in.console;

import java.util.Scanner;

import com.littlesekii.ai_vtuber.application.port.in.InteractionProviderPort;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionEvent;
import com.littlesekii.ai_vtuber.domain.interaction.InteractionType;


public class ConsoleInteractionAdapter implements InteractionProviderPort {

    private final Scanner scanner;
    private final String PROFILE_URL_PLACEHOLDER = "https://static.vecteezy.com/system/resources/thumbnails/048/496/223/small_2x/an-image-of-girl-listening-to-music-photo.jpeg";

    public ConsoleInteractionAdapter() {
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
                PROFILE_URL_PLACEHOLDER,
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
                PROFILE_URL_PLACEHOLDER,
                InteractionType.GIFT,
                5,
                500
            );
        }

        if (username.equalsIgnoreCase("follow")) {
            return new InteractionEvent(
                username,
                message,
                PROFILE_URL_PLACEHOLDER,
                InteractionType.FOLLOW,
                0,
                1000
            );
        }

        return new InteractionEvent(
            username,
            message,
            PROFILE_URL_PLACEHOLDER,
            InteractionType.MESSAGE,
            0,
            0
        );
    }
}