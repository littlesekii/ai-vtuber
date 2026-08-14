package com.littlesekii.ai_vtuber.avatar;

public class VnyanAvatarController {

    private final VnyanWebSocketClient client;

    public VnyanAvatarController() {
        this.client = new VnyanWebSocketClient(
            "ws://127.0.0.1:8067/vnyan"
        );
    }

    public void openMouth() {
        client.send("MouthOpen");
    }

    public void closeMouth() {
        client.send("MouthClose");
    }

    public void close() {
        client.close();
    }    
}
