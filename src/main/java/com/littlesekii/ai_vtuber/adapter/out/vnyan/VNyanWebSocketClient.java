package com.littlesekii.ai_vtuber.adapter.out.vnyan;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class VNyanWebSocketClient implements WebSocket.Listener {

    private final WebSocket webSocket;

    public VNyanWebSocketClient(String url) {
        HttpClient client = HttpClient.newHttpClient();

        this.webSocket = client.newWebSocketBuilder()
            .buildAsync(
                URI.create(url),
                this
            )
            .join();
    }

    public void send(String command) {
        webSocket.sendText(
            command,
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
        System.out.println("[VNYAN] WebSocket connected.");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(
        WebSocket webSocket,
        CharSequence data,
        boolean last
    ) {
        System.out.println("[VNYAN] Received: " + data);
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
        System.err.println("[VNYAN] WebSocket error:");
        error.printStackTrace();
    }
}