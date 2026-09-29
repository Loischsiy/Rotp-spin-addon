package com.loischsiy.rotpspin.client.render;

import net.minecraft.client.renderer.texture.NativeImage;

/**
 * Procedural wraparound skins for the math spheres (no PNG artist needed): the Wrecking Ball's dark
 * copper body with bright orange curved grooves; gold satellites and the dark sockets they leave
 * behind (references in docs/art/wrecking_ball.md). The green Steel Ball is NOT here: its wrap is a
 * PNG generated from the same geometry as its icon by docs/art/tools/gen_steel_ball.py.
 * Deterministic pixel math, not eyeballed art; a painted PNG can replace any of them with a one-line change in the renderer. Pure pixels, no GL calls.
 */
public final class SpinSphereTexture {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;
    /** Satellites and sockets are tiny: half-resolution wraps are plenty. */
    public static final int SMALL_WIDTH = 32;
    public static final int SMALL_HEIGHT = 16;
    /** Curved grooves around the Wrecking Ball body. */
    static final int GROOVES = 6;

    private SpinSphereTexture() {}

    /** Dark copper sphere with orange grooves, as in the anime/manga colour references. */
    public static NativeImage wreckingBall() {
        NativeImage image = new NativeImage(WIDTH, HEIGHT, false);
        for (int y = 0; y < HEIGHT; y++) {
            double v = (double) y / (HEIGHT - 1);
            double shade = shade(v);
            for (int x = 0; x < WIDTH; x++) {
                image.setPixelRGBA(x, y, copper((double) x / WIDTH, v, shade));
            }
        }
        return image;
    }

    /** Polished gold satellite ball with a soft highlight. */
    public static NativeImage satelliteBall() {
        NativeImage image = new NativeImage(SMALL_WIDTH, SMALL_HEIGHT, false);
        for (int y = 0; y < SMALL_HEIGHT; y++) {
            double v = (double) y / (SMALL_HEIGHT - 1);
            double shade = 0.55 + 0.45 * Math.sin(Math.PI * v);
            for (int x = 0; x < SMALL_WIDTH; x++) {
                double u = (double) x / SMALL_WIDTH;
                boolean highlight = u > 0.08 && u < 0.18 && v > 0.28 && v < 0.45;
                image.setPixelRGBA(x, y, highlight
                        ? rgba(255, 251, 233, 138)
                        : rgba(255, clamp((int) (235 * shade)), clamp((int) (199 * shade)), clamp((int) (49 * shade))));
            }
        }
        return image;
    }

    /** Dark recess left in the body after a satellite flies out. */
    public static NativeImage emptySocket() {
        NativeImage image = new NativeImage(SMALL_WIDTH, SMALL_HEIGHT, false);
        for (int y = 0; y < SMALL_HEIGHT; y++) {
            for (int x = 0; x < SMALL_WIDTH; x++) {
                image.setPixelRGBA(x, y, rgba(255, 40, 14, 4));
            }
        }
        return image;
    }

    /** Poles darker, equator lit (fake studio light from the top-left comes from normals). */
    private static double shade(double v) {
        return 0.45 + 0.55 * Math.sin(Math.PI * v);
    }

    private static int copper(double u, double v, double shade) {
        // Grooves are meridians with a slight twist, so they curve across the visible hemisphere.
        double lon = frac(u * GROOVES + 0.12 * Math.sin(2.0 * Math.PI * v));
        if (lon < 0.05) {
            // Hot centre line of the groove.
            return rgba(255, clamp((int) (242 * shade)), clamp((int) (159 * shade)), clamp((int) (108 * shade)));
        }
        if (lon < 0.14) {
            return rgba(255, clamp((int) (232 * shade)), clamp((int) (96 * shade)), clamp((int) (28 * shade)));
        }
        if (lon < 0.22) {
            // Shadowed edge of the groove.
            return rgba(255, clamp((int) (75 * shade)), clamp((int) (20 * shade)), 0);
        }
        return rgba(255, clamp((int) (137 * shade)), clamp((int) (63 * shade)), clamp((int) (16 * shade)));
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
