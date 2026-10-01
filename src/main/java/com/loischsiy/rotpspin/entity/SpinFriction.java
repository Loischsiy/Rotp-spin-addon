package com.loischsiy.rotpspin.entity;

/**
 * Friction of a spinning steel ball (docs/spin-lore.md, "Общие свойства"): the rotation strips the bark
 * off a trunk (SBR ch. 30). Pure math, no World access.
 */
public final class SpinFriction {
    private SpinFriction() {}

    /**
     * The ball strips bark only while it spins in its outgoing flight and still hits hard enough.
     * A returning ball has already spent its rotation on the way out.
     */
    public static boolean canStripBark(boolean enabled, boolean spinning, boolean returning,
            double speed, double minSpeed) {
        return enabled && spinning && !returning && speed >= minSpeed;
    }

    /**
     * Cutting bullets in metal (SBR ch. 44): same conditions as the bark, once per flight
     * (a ricochet does not cut twice).
     */
    public static boolean canCutBullets(boolean enabled, boolean spinning, boolean returning,
            boolean alreadyCut, double speed, double minSpeed) {
        return !alreadyCut && canStripBark(enabled, spinning, returning, speed, minSpeed);
    }

    /**
     * Chance that the cut eats the whole metal block. Each cut yields {@code bulletsPerCut} nuggets;
     * consuming the block with probability bulletsPerCut / nuggetsPerBlock keeps the expected yield
     * equal to the block's worth, so carving is never an iron dupe.
     */
    public static double blockConsumeChance(int bulletsPerCut, int nuggetsPerBlock) {
        if (nuggetsPerBlock <= 0) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, (double) bulletsPerCut / nuggetsPerBlock));
    }

    /** Speed left after the friction against the bark. */
    public static double speedAfterStrip(double speed, double retention) {
        return speed * Math.max(0.0, Math.min(1.0, retention));
    }
}
