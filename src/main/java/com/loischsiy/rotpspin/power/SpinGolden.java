package com.loischsiy.rotpspin.power;

import java.util.Collection;

/**
 * Golden Spin (lesson 4) and Super Spin (lesson 5): the ball damage bonus for a throw calibrated
 * by the golden ratio (docs/spin-lore.md, "Золотой Спин и среда"). Pure math, no World access:
 * the caller passes the biome category name and whether the thrower carries a calibration buckle.
 */
public final class SpinGolden {
    private SpinGolden() {}

    /** Golden ratio markers exist everywhere except the configured dead biome categories. */
    public static boolean isCalibrated(int lesson, String biomeCategory, boolean hasBuckle,
            Collection<? extends String> deadCategories) {
        return isCalibrated(lesson, biomeCategory, hasBuckle, false, deadCategories);
    }

    /**
     * Same as {@link #isCalibrated(int, String, boolean, Collection)}, plus a reference found or made
     * on the spot (docs/spin-lore.md, "Золотой Спин и среда", ch. 51-54): falling snowflakes, or
     * the golden rectangle framed by the user's own hands.
     */
    public static boolean isCalibrated(int lesson, String biomeCategory, boolean hasBuckle,
            boolean reference, Collection<? extends String> deadCategories) {
        if (lesson >= 5) {
            return true; // Super Spin: the detour, golden everywhere without calibration
        }
        if (lesson < 4) {
            return false;
        }
        return hasBuckle || reference || !deadCategories.contains(biomeCategory);
    }

    /** The golden rectangle framed by hands stays in the user's eye until {@code until} (game time). */
    public static boolean isHandFrameActive(long now, long until) {
        return now < until;
    }

    /** Game time until which a golden rectangle framed now stays calibrated. */
    public static long handFrameUntil(long now, int durationTicks) {
        return now + Math.max(0, durationTicks);
    }

    /** Damage multiplier of a spinning steel ball for the given lesson and calibration. */
    public static double multiplier(int lesson, boolean calibrated, double mult4, double mult5) {
        if (lesson >= 5) {
            return mult5;
        }
        if (lesson == 4 && calibrated) {
            return mult4;
        }
        return 1.0;
    }

    /**
     * A chipped (imperfect) ball keeps only a share of the bonus above x1: Ball Breaker's only
     * manga use was incomplete because of a damaged steel ball.
     */
    public static double applyChipped(double multiplier, double chippedRetention) {
        return 1.0 + (multiplier - 1.0) * chippedRetention;
    }

    /**
     * Super Spin from the saddle (lesson 5 "The shortest route is the detour"): a horse at its
     * natural gallop generates the golden rectangle energy itself, so the rider's throw is golden
     * without calibration. Needs the lesson (skill) and the speed, not the biome.
     */
    public static boolean isGallopSuperSpin(int lesson, double horseSpeed, double minGallopSpeed) {
        return lesson >= 4 && horseSpeed >= minGallopSpeed;
    }
}
