package io.github.amelonrind.darksky.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.amelonrind.darksky.ColorDimmer;
import net.minecraft.client.render.SkyRendering;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRendering.class)
public class MixinSkyRendering {

    @WrapOperation(method = "renderTopSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ColorHelper;toRgbaVector(I)Lorg/joml/Vector4f;"))
    public Vector4f mutateSkyColor(int argb, Operation<Vector4f> original) {
        return original.call(ColorDimmer.dimSkyColor(argb));
    }

}
