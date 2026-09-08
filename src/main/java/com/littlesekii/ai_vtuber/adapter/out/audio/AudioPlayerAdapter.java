package com.littlesekii.ai_vtuber.adapter.out.audio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import com.littlesekii.ai_vtuber.application.port.out.AudioPlayerProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.AvatarProviderPort;

public class AudioPlayerAdapter implements AudioPlayerProviderPort {

    private final AvatarProviderPort avatar;
    private final double mouthVolumeThreshold;
    private final int pollingIntervalMs;

    public AudioPlayerAdapter(
        AvatarProviderPort avatar,
        double mouthVolumeThreshold,
        int pollingIntervalMs
    ) {
        this.avatar = avatar;
        this.mouthVolumeThreshold = mouthVolumeThreshold;
        this.pollingIntervalMs = pollingIntervalMs;
    }

    public void play(Path audio) {
        AudioInputStream volumeStream = null;
        try {
            Path file = audio.toAbsolutePath();

            if (!Files.exists(file)) {
                throw new RuntimeException("Audio file not found: " + file);
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

            volumeStream = AudioSystem.getAudioInputStream(file.toFile());
            AudioFormat format = volumeStream.getFormat();
            long frameSize = format.getFrameSize();
            long frameRate = (long) format.getFrameRate();

            long framesRead = 0;
            byte[] buffer = new byte[1024];
            boolean mouthOpen = false;

            while (finished.getCount() > 0) {
                long positionMicros = clip.getMicrosecondPosition();
                long targetFrames = positionMicros * frameRate / 1_000_000;
                long deltaFrames = targetFrames - framesRead;

                if (deltaFrames > 0) {
                    long skipped = volumeStream.skip(deltaFrames * frameSize);
                    framesRead += skipped / frameSize;
                }

                int bytesRead = volumeStream.read(buffer);

                if (bytesRead > 0) {
                    framesRead += bytesRead / frameSize;
                }

                double volume = computeVolume(buffer, bytesRead);

                boolean shouldOpen = volume > mouthVolumeThreshold;

                if (shouldOpen != mouthOpen) {
                    if (shouldOpen) {
                        avatar.openMouth();
                    } else {
                        avatar.closeMouth();
                    }

                    mouthOpen = shouldOpen;
                }

                Thread.sleep(pollingIntervalMs);
            }

            avatar.closeMouth();

            clip.close();
            audioInput.close();
            
            Files.delete(file);

            System.out.println("[AUDIO] Finished.");

        } catch (Exception e) {
            avatar.closeMouth();

            throw new RuntimeException(
                "Could not play audio.",
                e
            );
        } finally {
            if (volumeStream != null) {
                try {
                    volumeStream.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private double computeVolume(byte[] buffer, int bytesRead) {
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
    }
}