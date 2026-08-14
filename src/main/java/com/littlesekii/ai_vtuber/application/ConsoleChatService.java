package com.littlesekii.ai_vtuber.application;

import java.util.Scanner;

import com.littlesekii.ai_vtuber.core.chat.ChatMessage;
import com.littlesekii.ai_vtuber.core.chat.ChatService;

public class ConsoleChatService implements ChatService {

    private final Scanner scanner;

    public ConsoleChatService() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public ChatMessage listen() {
        System.out.print("> ");
        String input = scanner.nextLine();
        return parse(input);
    }

    public ChatMessage parse(String input) {
        int separator = input.indexOf(':');
        if (separator == -1) {
            return new ChatMessage(
                "Viewer",
                input
            );
        }

        String username = input.substring(0, separator).trim();
        String message = input.substring(separator + 1).trim();

        return new ChatMessage(
            username,
            message
        );
    }
}
