package com.littlesekii.ai_vtuber.adapter.out.vnyan;

import com.littlesekii.ai_vtuber.application.port.out.AvatarProviderPort;

public class VNyanAvatarAdapter implements AvatarProviderPort  {

    private final VNyanWebSocketClient client;

    public VNyanAvatarAdapter(VNyanWebSocketClient client) {
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

    public void follow() {
        client.send("Follow");
    }

    public void close() {
        client.close();
    }    
}
