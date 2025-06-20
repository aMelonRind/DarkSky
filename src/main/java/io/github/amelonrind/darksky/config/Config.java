package io.github.amelonrind.darksky.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import io.github.amelonrind.darksky.ColorDimmer;
import io.github.amelonrind.darksky.DarkSky;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static io.github.amelonrind.darksky.DarkSky.LOGGER;

public class Config {
    public static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of(DarkSky.MOD_ID, "main"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(DarkSky.MOD_ID + ".json"))
                    .build())
            .build();

    public static Config get() {
        return HANDLER.instance();
    }

    @SerialEntry
    public boolean enabled = true;
    @SerialEntry
    public int skySat = 100;
    @SerialEntry
    public int fogSat = 100;
    @SerialEntry
    public int skyBri = -60;
    @SerialEntry
    public int fogBri = -60;

    public void apply() {
        skySat = clamp100200(skySat);
        fogSat = clamp100200(fogSat);
        skyBri = clamp100200(skyBri);
        fogBri = clamp100200(fogBri);
        ColorDimmer.skySatFactor = skySat / 100.0f;
        ColorDimmer.fogSatFactor = fogSat / 100.0f;
        ColorDimmer.skyBriFactor = skyBri / 100.0f;
        ColorDimmer.fogBriFactor = fogBri / 100.0f;

        DarkSky.enabled = enabled;
        LOGGER.info("[Dark Sky] applied config");
    }

    private static int clamp100200(int value) {
        return Math.min(Math.max(value, -100), 200);
    }

    public Config write(@NotNull Config other) {
        other.enabled = enabled;
        other.skySat = skySat;
        other.fogSat = fogSat;
        other.skyBri = skyBri;
        other.fogBri = fogBri;
        return this;
    }

}
