package io.github.amelonrind.darksky.mixin;

import io.github.amelonrind.darksky.ColorDimmer;
import net.minecraft.client.renderer.SkyRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SkyRenderer.class)
public class MixinSkyRenderer {
    @ModifyArg(method = "renderSkyDisc", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicGpuData;writeTransform(Lorg/joml/Matrix4f;Lorg/joml/Vector4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"))
    public Vector4f mutateSkyColor(Vector4f colorModulator) {
        return ColorDimmer.dimSkyColor(colorModulator);
    }
}
