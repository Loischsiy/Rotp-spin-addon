package com.loischsiy.rotpspin.power;

/**
 * Super Spin (docs/spin-lore.md, "Супер Спин (лошадь)"): a healthy horse at its natural gallop builds
 * the golden rectangle energy; any disturbance while riding (collision, an attack) breaks it
 * completely (SBR ch. 80, 85). The lesson 5 detour (ch. 85): a spinning ball on the horse's leg
 * makes it kick the rider, and the kick hands over the same energy without the gallop.
 * Pure math, no World access.
 */
public final class SpinSuperSpin {
    private static final int MAX_GALLOP_TICKS = 1_000_000;

    private SpinSuperSpin() {}

    /** Ticks of uninterrupted natural gallop after this tick. Any disturbance or a hurt horse resets it. */
    public static int nextGallopTicks(int ticks, boolean galloping, boolean disturbed, boolean horseHealthy) {
        if (!galloping || disturbed || !horseHealthy) {
            return 0;
        }
        return Math.min(ticks + 1, MAX_GALLOP_TICKS);
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
