package com.littlesekii.ai_vtuber.avatar;

public class VNyanAvatarController {

    private final VNyanWebSocketClient client;

    public VNyanAvatarController(VNyanWebSocketClient client) {
        this.client = client;
    }

    public void openMouth() {
        client.send("MouthOpen");
    }

    public void closeMouth() {
        client.send("MouthClose");
    }

    public void gift(int amount) {
        client.send("Gift " + amount);
    }

    public void close() {
        client.close();
    }    
}
