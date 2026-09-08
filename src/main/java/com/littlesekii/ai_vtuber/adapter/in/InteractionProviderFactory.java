package com.littlesekii.ai_vtuber.adapter.in;

import java.util.Locale;

import com.littlesekii.ai_vtuber.adapter.in.console.ConsoleInteractionAdapter;
import com.littlesekii.ai_vtuber.adapter.in.tiktok.TiktokInteractionAdapter;
import com.littlesekii.ai_vtuber.application.port.in.InteractionProviderPort;
import com.littlesekii.ai_vtuber.application.port.out.OverlayProviderPort;
import com.littlesekii.ai_vtuber.infra.config.AppConfig;
import com.littlesekii.ai_vtuber.infra.config.TiktokConfig;

public class InteractionProviderFactory {
    private InteractionProviderFactory() {
        /* This utility class should not be instantiated */
    }

    public static InteractionProviderPort create(
        AppConfig config,
        OverlayProviderPort overlayProviderPort
    ) {
        String inputType = config.getString("app.input.type", "tiktok").toLowerCase(Locale.ROOT);
        switch (inputType) {
            case "console":
                return new ConsoleInteractionAdapter();            
            case "tiktok":
            default:
                return new TiktokInteractionAdapter(
                    TiktokConfig.from(config), 
                    overlayProviderPort
                );
        }
    }
}
