package io.github.amelonrind.darksky.mixin;

import io.github.amelonrind.darksky.ColorDimmer;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @Inject(method = "getFogColor", at = @At(value = "TAIL"), cancellable = true)
    private void mutateFogColor(Camera camera, float tickProgress, ClientWorld world, int viewDistance, float skyDarkness, boolean thick, CallbackInfoReturnable<Vector4f> cir) {
        ColorDimmer.dimFogColor(cir);
    }

}
