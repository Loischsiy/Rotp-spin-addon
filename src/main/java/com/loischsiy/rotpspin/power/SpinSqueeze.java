package com.loischsiy.rotpspin.power;

/**
 * Pure math of the squeeze (docs/spin-lore.md: a spinning ball "flattens limbs and wrings water
 * out of the body", SBR ch. 2/20). Which limb a hit lands on and how long the effects last.
 */
public final class SpinSqueeze {

    /** Body zone hit by the ball. */
    public enum Zone { LEGS, BODY, HEAD }

    private SpinSqueeze() {
    }

    /**
     * Relative hit height: 0 at the feet, 1 at the top of the hitbox, clamped to [0, 1].
     * A target without height counts as hit in the body (0.5).
     */
    public static double relativeHeight(double hitY, double targetBottomY, double targetHeight) {
        if (!(targetHeight > 0)) {
            return 0.5;
        }
        double rel = (hitY - targetBottomY) / targetHeight;
        return Math.max(0.0, Math.min(1.0, rel));
    }

    /** Below legHeight: legs; up to headHeight: body and arms; above: head. */
    public static Zone zone(double relativeHeight, double legHeight, double headHeight) {
        if (relativeHeight < legHeight) {
            return Zone.LEGS;
        }
        if (relativeHeight < Math.max(legHeight, headHeight)) {
            return Zone.BODY;
        }
        return Zone.HEAD;
    }

    /** Effect duration in ticks; a chipped ball scales it by the damaged multiplier. Never negative. */
    public static int duration(int baseTicks, boolean chipped, double damagedMultiplier) {
        double ticks = Math.max(0, baseTicks) * (chipped ? Math.max(0.0, damagedMultiplier) : 1.0);
        return (int) Math.round(ticks);
    }

    /** The squeeze works only when enabled and the thrower has reached minLesson. */
    public static boolean applies(boolean enabled, int lesson, int minLesson) {
        return enabled && lesson >= minLesson;
    }
}
