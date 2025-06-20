package io.github.amelonrind.darksky;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// note for future me: don't use utility method from minecraft
// for example, they made ColorHelper.floatFromChannel(int) private, breaks 1.21.4
public class ColorDimmer {
    public static float skySatFactor = 1.0f;
    public static float fogSatFactor = 1.0f;
    public static float skyBriFactor = -0.6f;
    public static float fogBriFactor = -0.6f;

    public static final Color3f<Vector4f> lastFog = new Color3f<>(new Vector4f());
    public static final Color1i lastSky = new Color1i();

    public static void dimFogColor(CallbackInfoReturnable<Vector4f> cir) {
        if (!DarkSky.enabled) return;
        Vector4f vec = cir.getReturnValue();
        if (lastFog.checkEquality(vec.x, vec.y, vec.z) && lastFog.result.w == vec.w) {
            cir.setReturnValue(lastFog.result);
            return;
        }
        lastFog.result.set(vec.x, vec.y, vec.z);
        lastFog.result.w = vec.w;
        dimColor(vec.x, vec.y, vec.z, fogBriFactor, fogSatFactor,
                (r, g, b) -> cir.setReturnValue(lastFog.result.set(r, g, b))
        );
    }

    public static void dimSkyColor(CallbackInfoReturnable<Integer> cir) {
        if (!DarkSky.enabled) return;
        int color = cir.getReturnValueI();
        if (lastSky.checkEquality(color)) {
            cir.setReturnValue(lastSky.result);
            return;
        }
        lastSky.result = color;
        dimColor(lastSky.getR(), lastSky.getG(), lastSky.getB(), skyBriFactor, skySatFactor, (r, g, b) -> {
            lastSky.setResult(r, g, b);
            lastSky.result |= color & 0xFF000000;
            cir.setReturnValue(lastSky.result);
        });
    }

    public static void dimColor(float r, float g, float b, float briFactor, float satFactor, ColorConsumer consumer) {
        float max = Math.max(r, Math.max(g, b));
        if (max == 0) return;
        float min = Math.min(r, Math.min(g, b));

        float origSat = 1 - min / max;
        if (origSat > 0) {
            float satMul = calculateMultiplier(origSat, satFactor, max);
            r = max - (max - r) * satMul;
            g = max - (max - g) * satMul;
            b = max - (max - b) * satMul;
        }

        float briMul = calculateMultiplier(max, briFactor, max);
        consumer.accept(Math.max(0.001f, r * briMul), Math.max(0.001f, g * briMul), Math.max(0.001f, b * briMul));
    }

    private static float calculateMultiplier(float value, float factor, float impact) {
        return clamp01(value * (1.0f + factor * impact)) / value;
    }

    private static float clamp01(float value) {
        return value < 0.0f ? 0.0f : Math.min(value, 1.0f);
    }

    @FunctionalInterface
    public interface ColorConsumer {
        void accept(float r, float g, float b);
    }

    public static abstract class Color<T> {
        T result;

        Color(T result) {
            this.result = result;
        }

        public abstract void markDirty();

        public abstract int getColor();

        public abstract float getR();

        public abstract float getG();

        public abstract float getB();

    }

    public static class Color3f<T> extends Color<T> {
        float r = 0.0f;
        float g = 0.0f;
        float b = 0.0f;

        Color3f(T result) {
            super(result);
        }

        boolean checkEquality(float r, float g, float b) {
            if (this.r == r && this.g == g && this.b == b) return true;
            setColor(r, g, b);
            return false;
        }

        void setColor(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public void markDirty() {
            this.r = Float.NaN;
        }

        @Override
        public int getColor() {
            return ((int) (r * 255.0f) << 16) + ((int) (g * 255.0f) << 8) + (int) (b * 255.0f);
        }

        @Override
        public float getR() {
            return r;
        }

        @Override
        public float getG() {
            return g;
        }

        @Override
        public float getB() {
            return b;
        }

    }

    public static class Color1i extends Color<Integer> {
        boolean dirty = false;
        int argb = 0;

        Color1i() {
            super(0);
        }

        boolean checkEquality(int argb) {
            if (dirty) dirty = false;
            else if (this.argb == argb) return true;
            setColor(argb);
            return false;
        }

        void setColor(int argb) {
            this.argb = argb;
        }

        void setResult(float r, float g, float b) {
            // change 0 to 1 to prevent div by 0 issue in shader packs.
            int ir = Math.max(1, (int) (r * 255.0F));
            int ig = Math.max(1, (int) (g * 255.0F));
            int ib = Math.max(1, (int) (b * 255.0F));
            result = (ir << 16) + (ig << 8) + ib;
        }

        @Override
        public void markDirty() {
            dirty = true;
        }

        @Override
        public int getColor() {
            return argb & 0xFFFFFF;
        }

        @Override
        public float getR() {
            return (float) ((argb >> 16) & 0xFF) / 255.0f;
        }

        @Override
        public float getG() {
            return (float) ((argb >> 8) & 0xFF) / 255.0f;
        }

        @Override
        public float getB() {
            return (float) (argb & 0xFF) / 255.0f;
        }

    }

}
