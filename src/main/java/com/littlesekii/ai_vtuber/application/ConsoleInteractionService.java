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
                input
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
                InteractionType.GIFT
            );
        }

        if (username.equalsIgnoreCase("follow")) {
            return new InteractionEvent(
                username,
                message,
                InteractionType.FOLLOW
            );
        }

        return new InteractionEvent(
            username,
            message
        );
    }
}