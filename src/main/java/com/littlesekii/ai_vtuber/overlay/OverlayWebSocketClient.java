package com.littlesekii.ai_vtuber.overlay;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.littlesekii.ai_vtuber.config.AppConfig;

public class OverlayWebSocketClient implements WebSocket.Listener {

    private final WebSocket webSocket;
    private final ObjectMapper objectMapper;

    public OverlayWebSocketClient() {
        HttpClient client = HttpClient.newHttpClient();

        objectMapper = new ObjectMapper();

        this.webSocket = client.newWebSocketBuilder()
            .buildAsync(
                URI.create(AppConfig.OVERLAY_WEB_SOCKET),
                this
            )
            .join();
    }

    public void send(String type, Object data) {
        ObjectNode message = objectMapper.createObjectNode();

        message.put("type", type);
        message.set(
            "data",
            objectMapper.valueToTree(data)
        );

        webSocket.sendText(
            message.toString(),
            true
        ).join();
    }

    public void send(String message) {
        webSocket.sendText(
            message,
            true
        ).join();
    }

    public void close() {
        webSocket.sendClose(
            WebSocket.NORMAL_CLOSURE,
            "bye"
        ).join();
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println(
            "[OVERLAY] WebSocket connected."
        );

        WebSocket.Listener.super.onOpen(
            webSocket
        );
    }

    @Override
    public CompletionStage<?> onText(
        WebSocket webSocket,
        CharSequence data,
        boolean last
    ) {
        System.out.println(
            "[OVERLAY] Received: " + data
        );

        return WebSocket.Listener.super.onText(
            webSocket,
            data,
            last
        );
    }

    @Override
    public void onError(
        WebSocket webSocket,
        Throwable error
    ) {
        System.err.println(
            "[OVERLAY] WebSocket error:"
        );

        error.printStackTrace();
    }
}