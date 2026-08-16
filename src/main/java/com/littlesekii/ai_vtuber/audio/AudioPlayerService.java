package com.littlesekii.ai_vtuber.audio;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import com.littlesekii.ai_vtuber.avatar.VNyanAvatarController;
import com.littlesekii.ai_vtuber.tts.GeneratedAudio;

public class AudioPlayerService {

    private final VNyanAvatarController avatar;

    public AudioPlayerService(VNyanAvatarController avatar) {
        this.avatar = avatar;
    }

    public void process(GeneratedAudio audio) {
        try {
            Path file = audio.file().toAbsolutePath();

            if (!Files.exists(file)) {
                throw new RuntimeException(
                    "Audio file not found: " + file
                );
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(
                file.toFile()
            );

            Clip clip = AudioSystem.getClip();

            CountDownLatch finished = new CountDownLatch(1);

            clip.addLineListener(eventListener -> {
                if (eventListener.getType() == LineEvent.Type.STOP) {
                    finished.countDown();
                }
            });

            clip.open(audioInput);

            System.out.println("[AUDIO] Playing...");

            clip.start();

            boolean mouthOpen = false;

            while (finished.getCount() > 0) {
                long position = clip.getMicrosecondPosition();

                double volume = getVolumeAt(
                    file,
                    position
                );

                boolean shouldOpen = volume > 0.015;

                if (shouldOpen != mouthOpen) {
                    if (shouldOpen) {
                        avatar.openMouth();
                    } else {
                        avatar.closeMouth();
                    }

                    mouthOpen = shouldOpen;
                }

                Thread.sleep(30);
            }

            avatar.closeMouth();

            clip.close();
            audioInput.close();

            System.out.println("[AUDIO] Finished.");

        } catch (Exception e) {
            avatar.closeMouth();

            throw new RuntimeException(
                "Could not play audio.",
                e
            );
        }
    }

    private double getVolumeAt(Path file, long microsecondPosition) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(
                file.toFile()
            );

            var format = stream.getFormat();

            long framePosition =
                microsecondPosition *
                (long) format.getFrameRate() /
                1_000_000;

            long bytesToSkip =
                framePosition *
                format.getFrameSize();

            stream.skip(bytesToSkip);

            byte[] buffer = new byte[1024];

            int bytesRead = stream.read(buffer);

            stream.close();

            if (bytesRead <= 0) {
                return 0;
            }

            double sum = 0;

            for (int i = 0; i < bytesRead - 1; i += 2) {
                int sample =
                    (buffer[i] & 0xff) |
                    (buffer[i + 1] << 8);

                double normalized =
                    sample / 32768.0;

                sum += normalized * normalized;
            }

            return Math.sqrt(
                sum / (bytesRead / 2.0)
            );

        } catch (Exception e) {
            return 0;
        }
    }
}