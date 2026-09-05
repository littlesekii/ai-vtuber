package com.littlesekii.ai_vtuber.application.port.out;

public interface AvatarProviderPort {
    void openMouth();
    void closeMouth();
    void follow();
    void gift(int amount);
    void close();
}
