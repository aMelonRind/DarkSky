package io.github.amelonrind.darksky;

import org.joml.Vector4f;

// note for future me: don't use utility method from minecraft
// for example, they made ColorHelper.floatFromChannel(int) private, breaks 1.21.4
public class ColorDimmer {
    private static float skySatFactor = 1.0f;
    private static float fogSatFactor = 1.0f;
    private static float skyBriFactor = -0.6f;
    private static float fogBriFactor = -0.6f;

    public static final Color3f<Vector4f> lastFog = new Color3f<>(new Vector4f());
    public static final Color3f<Vector4f> lastSky = new Color3f<>(new Vector4f());

    public static void onConfig(int skySat, int fogSat, int skyBri, int fogBri) {
        skySatFactor = skySat / 100.0f;
        fogSatFactor = fogSat / 100.0f;
        skyBriFactor = skyBri / 100.0f;
        fogBriFactor = fogBri / 100.0f;
    }

    public static void dimFogColor(Vector4f dest) {
        if (!DarkSky.enabled) return;
        if (lastFog.isChanged(dest.x, dest.y, dest.z)) {
            dimColor(dest.x, dest.y, dest.z, fogBriFactor, fogSatFactor, lastFog.result::set);
        }
        dest.set(lastFog.result);
    }

    public static Vector4f dimSkyColor(Vector4f color) {
        if (!DarkSky.enabled) return color;
        if (lastSky.isChanged(color.x, color.y, color.z)) {
            dimColor(color.x, color.y, color.z, skyBriFactor, skySatFactor, lastSky.result::set);
        }
        color.set(lastSky.result);
        return color;
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
        consumer.accept(
                Math.max(0.001f, r * briMul),
                Math.max(0.001f, g * briMul),
                Math.max(0.001f, b * briMul)
        );
    }

    // impact: tends to lower effect on already-dark color
    private static float calculateMultiplier(float value, float factor, float impact) {
        return Math.clamp(value * (1.0f + factor * impact), 0.0f, 1.0f) / value;
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

        boolean isChanged(float r, float g, float b) {
            if (this.r == r && this.g == g && this.b == b) return false;
            setColor(r, g, b);
            return true;
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
}
