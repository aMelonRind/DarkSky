package io.github.amelonrind.darksky.mixin;

import io.github.amelonrind.darksky.ColorDimmer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(method = "getSkyColor", at = @At("TAIL"), cancellable = true)
    public void mutateSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        ColorDimmer.dimSkyColor(cir);
    }

}
