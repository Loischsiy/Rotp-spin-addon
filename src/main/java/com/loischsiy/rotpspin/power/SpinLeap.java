package com.loischsiy.rotpspin.power;

/**
 * Lesson 1 "If there is a will, do it": the Spin leap is driven by will, not by leg strength,
 * so slowness does not weaken it. Pure math, no World access.
 */
public final class SpinLeap {
    // Below this speed ratio the user is effectively immobile; avoids dividing by ~0.
    static final double MIN_SPEED_RATIO = 0.05;

    private SpinLeap() {}

    /**
     * RotP multiplies the leap strength by {@code speed / baseSpeed} afterwards
     * ({@code NonStandPower#leapStrength}). Pre-divides by that ratio when it is below 1,
     * so after RotP's multiplication the leap is at least {@code baseStrength}. Speed boosts still help.
     */
    public static float strength(float baseStrength, double speed, double baseSpeed, boolean ignoreSlowness) {
        if (!ignoreSlowness || baseSpeed <= 0) {
            return baseStrength;
        }
        double ratio = speed / baseSpeed;
        if (ratio >= 1) {
            return baseStrength;
        }
        return (float) (baseStrength / Math.max(ratio, MIN_SPEED_RATIO));
    }
}
