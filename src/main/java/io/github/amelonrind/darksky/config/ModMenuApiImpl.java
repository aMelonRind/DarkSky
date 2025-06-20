package io.github.amelonrind.darksky.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import io.github.amelonrind.darksky.ColorDimmer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.github.amelonrind.darksky.ColorDimmer.*;

public class ModMenuApiImpl implements ModMenuApi {
    private static final Text TEXT_NONE = Text.of("None");
    private static final CompletableFuture<Optional<ImageRenderer>> skyImage = new CompletableFuture<>();
    private static final CompletableFuture<Optional<ImageRenderer>> fogImage = new CompletableFuture<>();
    private static final ColorPreview skyColorPreview = new ColorPreview();
    private static final ColorPreview fogColorPreview = new ColorPreview();
    private static final Config cfg4preview = new Config();

    private static final Config def = Config.HANDLER.defaults();
    private static Config cfg = Config.get();

    static {
        skyImage.complete(Optional.of(skyColorPreview));
        fogImage.complete(Optional.of(fogColorPreview));
    }

    @Contract(value = "_ -> new", pure = true)
    private static @NotNull Text translate(String key) {
        return Text.translatable("darkSky.config.entry." + key);
    }

    private static Option<Integer> optionOf(String nameKey, Function<Config, Integer> getter, BiConsumer<Config, Integer> setter, boolean isSky) {
        return Option.<Integer>createBuilder()
                .name(translate(nameKey))
                .binding(getter.apply(def), () -> getter.apply(cfg), val -> setter.accept(cfg, val))
                .description(val -> {
                    setter.accept(cfg4preview, val);
                    if (isSky) {
                        skyColorPreview.setColor(lastSky.getR(), lastSky.getG(), lastSky.getB());
                        ColorDimmer.dimColor(lastSky.getR(), lastSky.getG(), lastSky.getB(),
                                cfg4preview.skyBri / 100.0f, cfg4preview.skySat / 100.0f,
                                skyColorPreview::setColor
                        );
                        return OptionDescription.createBuilder()
                                .text(formatColor(lastSky.getColor(), skyColorPreview.getColor()))
                                .customImage(skyImage)
                                .build();
                    } else {
                        fogColorPreview.setColor(lastFog.getR(), lastFog.getG(), lastFog.getB());
                        ColorDimmer.dimColor(lastFog.getR(), lastFog.getG(), lastFog.getB(),
                                cfg4preview.fogBri / 100.0f, cfg4preview.fogSat / 100.0f,
                                fogColorPreview::setColor
                        );
                        return OptionDescription.createBuilder()
                                .text(formatColor(lastFog.getColor(), fogColorPreview.getColor()))
                                .customImage(fogImage)
                                .build();
                    }
                })
                .controller(option -> IntegerSliderControllerBuilder.create(option)
                        .range(-100, 200)
                        .step(1)
                        .formatValue(v -> v == 0 ? TEXT_NONE : Text.of((v > 0 ? "+" : "") + v + "%")))
                .build();
    }

    @Contract("_, _ -> new")
    private static @NotNull Text formatColor(int from, int to) {
        return Text.literal(String.format("#%06X -> #%06X", from, to));
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return p -> {
            cfg = Config.get().write(cfg4preview);
            Text title = Text.translatable("darkSky.config.title");
            return YetAnotherConfigLib.createBuilder()
                    .title(title)
                    .category(ConfigCategory.createBuilder()
                            .name(title)
                            .option(Option.<Boolean>createBuilder()
                                    .name(translate("enabled"))
                                    .binding(def.enabled, () -> cfg.enabled, val -> cfg.enabled = val)
                                    .controller(TickBoxControllerBuilder::create)
                                    .build())
                            .option(optionOf("skySatFactor", cfg -> cfg.skySat, (cfg, val) -> cfg.skySat = val, true))
                            .option(optionOf("fogSatFactor", cfg -> cfg.fogSat, (cfg, val) -> cfg.fogSat = val, false))
                            .option(optionOf("skyBriFactor", cfg -> cfg.skyBri, (cfg, val) -> cfg.skyBri = val, true))
                            .option(optionOf("fogBriFactor", cfg -> cfg.fogBri, (cfg, val) -> cfg.fogBri = val, false))
                            .build())
                    .save(() -> {
                        cfg.apply();
                        Config.HANDLER.save();
                        lastFog.markDirty();
                        lastSky.markDirty();
                    })
                    .build()
                    .generateScreen(p);
        };
    }

    public static class ColorPreview implements ImageRenderer {
        private static final MinecraftClient mc = MinecraftClient.getInstance();
        public int color = 0;

        public void setColor(float r, float g, float b) {
            int ir = (int) (r * 255);
            int ig = (int) (g * 255);
            int ib = (int) (b * 255);
            color = (ir << 16) + (ig << 8) + ib;
        }

        public int getColor() {
            return color;
        }

        @Override
        public int render(@NotNull DrawContext context, int x, int y, int renderWidth, float tickDelta) {
            // I can't figure out how to render a square, so I rendered a square character instead.
            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0);
            float scale = (float) renderWidth / 8.0f;
            boolean doubleWidth = mc.options.getForceUnicodeFont().getValue();
            context.getMatrices().scale(doubleWidth ? scale * 2 : scale, scale, 1.0f);
            context.drawText(mc.textRenderer, "█", 0, 0, color, false);
            context.getMatrices().pop();
            return renderWidth;
        }

        @Override
        public void close() {}

    }

}