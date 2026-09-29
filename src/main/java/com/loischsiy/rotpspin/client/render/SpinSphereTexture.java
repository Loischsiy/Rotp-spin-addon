package com.loischsiy.rotpspin.client.render;

import net.minecraft.client.renderer.texture.NativeImage;

/**
 * Procedural wraparound skin for the math sphere (no PNG artist needed): brushed steel with a
 * sine engraving that makes the rotation visible, plus a brass equatorial band on the guard
 * version. Deterministic pixel math, not eyeballed art; a painted PNG can replace it later
 * with a one-line change in the renderer. Pure pixels, no GL calls.
 */
public final class SpinSphereTexture {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;

    private SpinSphereTexture() {}

    public static NativeImage steelBall() {
        return paint(false);
    }

    public static NativeImage wreckingBall() {
        return paint(true);
    }

    static NativeImage paint(boolean wrecking) {
        NativeImage image = new NativeImage(WIDTH, HEIGHT, false);
        for (int y = 0; y < HEIGHT; y++) {
            double v = (double) y / (HEIGHT - 1);
            // Poles darker, equator lit (fake studio light from the top-left comes from normals).
            double shade = 0.45 + 0.55 * Math.sin(Math.PI * v);
            for (int x = 0; x < WIDTH; x++) {
                double u = (double) x / WIDTH;
                int color;
                if (wrecking && Math.abs(v - 0.5) < 0.07) {
                    color = brass(u, v, shade);
                }
                else {
                    color = steel(u, v, shade);
                }
                image.setPixelRGBA(x, y, color);
            }
        }
        return image;
    }

    private static int steel(double u, double v, double shade) {
        // Spiral engraving: varies with longitude so the spin reads in flight.
        double groove = frac(u * 4.0 + v * 1.5);
        double dark = groove < 0.05 ? 0.55 : 1.0;
        int base = (int) (148 * shade * dark);
        int light = (int) (222 * shade * dark);
        // Brushed bands along latitude.
        int band = (int) (8 * Math.sin(v * Math.PI * 24));
        return rgba(255,
                clamp(base + band + (u < 0.25 ? (light - base) / 2 : 0)),
                clamp(base + band + 6),
                clamp(light + band - 40));
    }

    private static int brass(double u, double v, double shade) {
        // Rivets around the band.
        boolean rivet = Math.abs(v - 0.5) < 0.02 && frac(u * 8.0) < 0.06;
        if (rivet) {
            return rgba(255, 240, 200, 110);
        }
        // Band edges are dark lines.
        double edge = Math.abs(v - 0.5) > 0.055 ? 0.5 : 1.0;
        return rgba(255,
                clamp((int) (184 * shade * edge)),
                clamp((int) (134 * shade * edge)),
                clamp((int) (11 * shade + 20 * edge)));
    }

    /** NativeImage.combine takes (alpha, blue, green, red) in 1.16.5: keep call sites readable. */
    private static int rgba(int alpha, int red, int green, int blue) {
        return NativeImage.combine(alpha, blue, green, red);
    }

    private static double frac(double value) {
        return value - Math.floor(value);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
