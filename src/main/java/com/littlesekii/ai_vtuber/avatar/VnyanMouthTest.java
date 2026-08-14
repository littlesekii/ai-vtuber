package com.littlesekii.ai_vtuber.avatar;

public class VnyanMouthTest {

    public static void main(String[] args)
        throws Exception {

        VnyanAvatarController avatar =
            new VnyanAvatarController();

        Thread.sleep(1000);

        System.out.println("Abrindo boca...");
        avatar.openMouth();

        Thread.sleep(2000);

        System.out.println("Fechando boca...");
        avatar.closeMouth();

        Thread.sleep(500);

        avatar.close();
    }
}