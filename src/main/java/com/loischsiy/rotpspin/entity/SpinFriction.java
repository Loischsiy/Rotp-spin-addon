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

    /** Speed left after the friction against the bark. */
    public static double speedAfterStrip(double speed, double retention) {
        return speed * Math.max(0.0, Math.min(1.0, retention));
    }
}
