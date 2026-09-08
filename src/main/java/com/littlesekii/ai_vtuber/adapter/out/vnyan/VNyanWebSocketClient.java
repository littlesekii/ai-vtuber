package com.littlesekii.ai_vtuber.adapter.out.vnyan;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class VNyanWebSocketClient implements WebSocket.Listener {

    private final String url;
    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>(null);

    public VNyanWebSocketClient(String url) {
        this.url = url;
        connect();
    }

    private void connect() {
        HttpClient client = HttpClient.newHttpClient();
        try {
            WebSocket ws = client.newWebSocketBuilder()
                .buildAsync(URI.create(url), this)
                .get(5, TimeUnit.SECONDS);
            webSocketRef.set(ws);
            System.out.println("[VNYAN] WebSocket connected to " + url);
        } catch (Exception e) {
            System.err.println("[VNYAN] Could not connect to " + url + ": " + e.getMessage());
        }
    }

    private void reconnect() {
        WebSocket old = webSocketRef.getAndSet(null);
        if (old != null) {
            try {
                old.sendClose(WebSocket.NORMAL_CLOSURE, "reconnect").join();
            } catch (Exception ignored) {}
        }
        System.out.println("[VNYAN] Attempting reconnect...");
        connect();
    }

    public void send(String command) {
        WebSocket ws = webSocketRef.get();
        if (ws == null) {
            reconnect();
            ws = webSocketRef.get();
            if (ws == null) {
                System.err.println("[VNYAN] Still not connected, dropping command: " + command);
                return;
            }
        }
        try {
            ws.sendText(command, true).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("[VNYAN] Send failed: " + e.getMessage() + ". Reconnecting...");
            reconnect();
        }
    }

    public void close() {
        WebSocket ws = webSocketRef.get();
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye").get(3, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }
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
    public CompletionStage<?> onClose(
        WebSocket webSocket,
        int statusCode,
        String reason
    ) {
        System.out.println("[VNYAN] WebSocket closed: " + statusCode + " " + reason);
        webSocketRef.set(null);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(
        WebSocket webSocket,
        Throwable error
    ) {
        System.err.println("[VNYAN] WebSocket error: " + error.getMessage());
        webSocketRef.set(null);
    }
}
