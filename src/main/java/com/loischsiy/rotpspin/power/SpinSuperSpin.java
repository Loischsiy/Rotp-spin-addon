package com.loischsiy.rotpspin.power;

/**
 * Super Spin (docs/spin-lore.md, "Супер Спин (лошадь)"): a healthy horse at its natural gallop builds
 * the golden rectangle energy; a real disturbance while riding (a crash, an attack) breaks it
 * completely (SBR ch. 80, 85), uneven Minecraft terrain alone does not. The lesson 5 detour (ch. 85): a spinning ball on the horse's leg
 * makes it kick the rider, and the kick hands over the same energy without the gallop.
 * Pure math, no World access.
 */
public final class SpinSuperSpin {
    private static final int MAX_GALLOP_TICKS = 1_000_000;

    private SpinSuperSpin() {}

    /** Weight of the newest tick in the smoothed horse speed. */
    public static final double SPEED_SMOOTHING = 0.3;

    /**
     * Smoothed horizontal speed of the horse (exponential moving average). A player-ridden horse is
     * moved by client packets, which arrive unevenly: one tick shows 0, the next twice the distance.
     * Hills, step-ups and jumps also make the per-tick distance jitter.
     */
    public static double smoothSpeed(double smoothed, double raw) {
        return smoothed + (raw - smoothed) * SPEED_SMOOTHING;
    }

    /**
     * A crash: the horse ran into something and actually stopped. Sliding along a wall, stepping up a
     * block or a lagging packet is not a crash, only a real stop against an obstacle.
     */
    public static boolean isCrash(boolean horizontalCollision, double rawSpeed, double smoothedSpeed,
            double stopFraction) {
        return horizontalCollision && rawSpeed < smoothedSpeed * stopFraction;
    }

    /** Ticks in a row the horse has been below gallop speed. */
    public static int nextSlowTicks(int slowTicks, boolean galloping) {
        return galloping ? 0 : Math.min(slowTicks + 1, MAX_GALLOP_TICKS);
    }

    /**
     * Ticks of natural gallop after this tick. A brief slow-down (a hill, a turn, a jump) up to
     * {@code graceTicks} keeps the build-up without adding to it; a longer stop, a crash, an attack or
     * a hurt horse breaks it completely (SBR ch. 80, 85).
     */
    public static int nextGallopTicks(int ticks, boolean galloping, int slowTicks, int graceTicks,
            boolean broken, boolean horseHealthy) {
        if (broken || !horseHealthy || slowTicks > Math.max(0, graceTicks)) {
            return 0;
        }
        return galloping ? Math.min(ticks + 1, MAX_GALLOP_TICKS) : ticks;
    }

    public static boolean isGallopReady(int ticks, int requiredTicks) {
        return ticks >= Math.max(1, requiredTicks);
    }

    /** A healthy horse: health share at least {@code minFraction}. */
    public static boolean isHealthy(float health, float maxHealth, double minFraction) {
        return maxHealth > 0 && health / maxHealth >= minFraction;
    }

    /**
     * The detour works for a lesson 5 user, a perfect sphere (a chipped ball fails, ch. 84),
     * the user's own healthy horse and a rider close enough to be kicked.
     */
    public static boolean canDetour(int lesson, boolean chipped, boolean ownHorse, boolean horseHealthy,
            double riderDistanceSq, double kickRange) {
        return lesson >= 5 && !chipped && ownHorse && horseHealthy
                && riderDistanceSq <= kickRange * kickRange;
    }

    /** Game time until which the energy of the kick stays in the user. */
    public static long detourUntil(long now, int durationTicks) {
        return now + Math.max(0, durationTicks);
    }

    /** Ball multiplier with Super Spin: never lower than what the user already had. */
    public static double multiplier(boolean superSpin, double base, double superMultiplier) {
        return superSpin ? Math.max(base, superMultiplier) : base;
    }
}
