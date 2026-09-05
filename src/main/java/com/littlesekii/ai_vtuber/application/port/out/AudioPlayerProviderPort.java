package com.littlesekii.ai_vtuber.application.port.out;

import java.nio.file.Path;

public interface AudioPlayerProviderPort {
    void play(Path audio);
}
