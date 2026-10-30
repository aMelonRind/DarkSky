package io.github.amelonrind.darksky;

import io.github.amelonrind.darksky.config.Config;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DarkSky implements ClientModInitializer {
    public static final String MOD_ID = "dark-sky";
    public static final Logger LOGGER = LogManager.getLogger(DarkSky.class);

    public static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        Config.HANDLER.load();
        Config.get().apply();
    }
}
