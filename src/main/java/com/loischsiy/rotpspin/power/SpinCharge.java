package com.loischsiy.rotpspin.power;

/**
 * Pure math of the held Spin charge of a steel ball (no World, covered by JUnit).
 * The longer the user winds the rotation up in the hand, the faster and harder the throw
 * (Spin raises a projectile's destructive power, docs/spin-lore.md, ch. 9). An imperfect
 * (chipped) sphere cannot take the higher rotation (ch. 84), so its charge is capped.
 */
public final class SpinCharge {
    private static final double EPS = 1e-9;

    private SpinCharge() {}

    /** Share of the full charge reached after {@code chargeTicks}, clamped to 0..1; 1 if {@code maxTicks <= 0}. */
    public static double fraction(int chargeTicks, int maxTicks) {
        if (maxTicks <= 0) {
            return 1.0;
        }
        return clamp01(chargeTicks / (double) maxTicks);
    }

    /** Highest share a ball can take: 1 for a perfect sphere, {@code chippedMax} for a chipped one. */
    public static double limit(boolean chipped, double chippedMax) {
        return chipped ? clamp01(chippedMax) : 1.0;
    }

    /** {@code fraction} limited by the ball's state. */
    public static double cap(double fraction, boolean chipped, double chippedMax) {
        return Math.min(clamp01(fraction), limit(chipped, chippedMax));
    }

    /** Linear multiplier from 1 (no charge) to {@code maxMultiplier} (full charge); never below 1. */
    public static double multiplier(double fraction, double maxMultiplier) {
        return 1.0 + Math.max(0.0, maxMultiplier - 1.0) * clamp01(fraction);
    }

    /** Whether the charge cannot grow any more for this ball. */
    public static boolean isFull(int chargeTicks, int maxTicks, boolean chipped, double chippedMax) {
        return fraction(chargeTicks, maxTicks) >= limit(chipped, chippedMax) - EPS;
    }

    /** Charge in whole percent for the HUD message. */
    public static int percent(double fraction) {
        return (int) Math.round(clamp01(fraction) * 100.0);
    }

    private static double clamp01(double v) {
        return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
    }
}
