package com.loischsiy.rotpspin.power;

/**
 * Point-blank strike with a spinning steel ball held in the hand (action {@code spin_ball_strike}).
 * Lore (docs/spin-lore.md): the rotation is passed on by touch (lesson 1, ch. 10) and its energy goes into
 * nearby objects as vibrations (ch. 5), so the touched body is thrown back. The strike itself is not a
 * scene from the manga: gameplay assumption built on canon properties. Pure math, no World access.
 */
public final class SpinStrike {

    private SpinStrike() {}

    /**
     * Share of the strike a ball keeps: a perfect sphere gives all of it, a chipped (imperfect) ball only
     * {@code chippedMultiplier}, clamped to [0, 1] so a chipped ball is never stronger than a perfect one.
     */
    public static double efficiency(boolean chipped, double chippedMultiplier) {
        if (!chipped) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, chippedMultiplier));
    }

    /** A configured value (damage, knockback, lift) scaled by the ball's {@link #efficiency}; never negative. */
    public static double scaled(double base, boolean chipped, double chippedMultiplier) {
        return Math.max(0.0, base) * efficiency(chipped, chippedMultiplier);
    }

    /** Vertical speed after the strike: the push lifts the target at least to {@code lift}, never pulls it down. */
    public static double liftedY(double currentY, double lift) {
        return Math.max(currentY, lift);
    }
}
