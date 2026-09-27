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
        if (lesson >= 5) {
            return true; // Super Spin: the detour, golden everywhere without calibration
        }
        if (lesson < 4) {
            return false;
        }
        return hasBuckle || !deadCategories.contains(biomeCategory);
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
}
