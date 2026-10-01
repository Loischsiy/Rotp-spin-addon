package com.loischsiy.rotpspin.client.render;

/**
 * Motion of Gyro's worn cloak: a shoulder piece with three hanging segments (docs/art/gyros_cloak.md).
 * Pure math in degrees, no Minecraft classes. Purely visual, so the constants live here, not in SpinConfig.
 * <ul>
 * <li>drag: the cloth trails behind motion like the vanilla cape (same inputs as CapeLayer);</li>
 * <li>flutter: each segment waves with its own phase, stronger when moving;</li>
 * <li>sail: {@code open} 0..1 lifts the segments back and fans the outer ones out.</li>
 * </ul>
 */
public final class CloakAnim {
    public static final int SEGMENTS = 3;
    static final float MAX_LIFT = 80.0F;
    static final float MAX_PITCH = 95.0F;
    static final float SAIL_LIFT = 72.0F;
    static final float SAIL_SPREAD = 24.0F;
    static final float IDLE_FLUTTER = 2.5F;
    static final float MOVE_FLUTTER = 6.0F;
    static final float SAIL_FLUTTER = 8.0F;
    static final float IDLE_SPEED = 0.15F;
    static final float SAIL_SPEED = 0.7F;
    static final float PHASE_STEP = 2.1F;
    /** The sail opens or folds fully in 5 ticks. */
    public static final float OPEN_PER_TICK = 0.2F;

    private CloakAnim() {}

    /**
     * How far the air pushes the cloth back (vanilla CapeLayer formula; its crouch term is dropped
     * because the cloak already copies the torso pose).
     * @param back cloth lag behind the body along its facing (blocks per tick)
     * @param rise cloth lag above the body (blocks per tick, positive while falling)
     * @param bob  view bobbing amount
     * @param walk walked distance (the step rhythm)
     */
    public static float drag(double back, double rise, float bob, float walk) {
        float b = clamp((float) back * 100.0F, 0.0F, 150.0F);
        float r = clamp((float) rise * 10.0F, -6.0F, 32.0F);
        r += (float) Math.sin(walk * 6.0F) * 32.0F * bob;
        return clamp(6.0F + b / 2.0F + r, 0.0F, MAX_LIFT);
    }

    /** Sideways lean from sideways cloth lag (vanilla: half of the clamped lag). */
    public static float lean(double side) {
        return clamp((float) side * 100.0F, -20.0F, 20.0F) / 2.0F;
    }

    /** Back lift of segment 0..2 (positive = the cloth swings away from the back). */
    public static float pitch(int segment, float drag, float open, float ageTicks) {
        float base = lerp(open, drag, Math.max(drag, SAIL_LIFT));
        float amp = lerp(open, IDLE_FLUTTER + MOVE_FLUTTER * drag / MAX_LIFT, SAIL_FLUTTER);
        float speed = lerp(open, IDLE_SPEED, SAIL_SPEED);
        return clamp(base + amp * (float) Math.sin(ageTicks * speed + segment * PHASE_STEP), 0.0F, MAX_PITCH);
    }

    /** Roll of segment 0..2: the sail fans the outer segments away from the middle; all follow the lean. */
    public static float roll(int segment, float lean, float open) {
        return (1 - segment) * SAIL_SPREAD * open + lean;
    }

    /** Moves {@code current} towards {@code target} by at most {@code maxStep}. */
    public static float approach(float current, float target, float maxStep) {
        if (current < target) {
            return Math.min(target, current + maxStep);
        }
        return Math.max(target, current - maxStep);
    }

    static float lerp(float t, float a, float b) {
        return a + (b - a) * t;
    }

    static float clamp(float v, float min, float max) {
        return v < min ? min : v > max ? max : v;
    }
}
