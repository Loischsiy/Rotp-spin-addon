package com.loischsiy.rotpspin.power;

/**
 * Spin resonance (docs/spin-lore.md, "Общие свойства", SBR ch. 23): an object that already
 * carries Spin amplifies the Spin of other projectiles and extends the range of the balls.
 * Every spinning projectile near the thrower at the moment of the throw is one source.
 * Pure math, no World access, for JUnit.
 */
public final class SpinResonance {

    private SpinResonance() {}

    /** Sources that count, never negative and never above {@code maxSources}. */
    public static int effectiveSources(int sources, int maxSources) {
        return Math.max(0, Math.min(sources, Math.max(0, maxSources)));
    }

    /** Damage multiplier: {@code 1 + n * perSource}, capped at {@code maxMultiplier}, never below 1. */
    public static double damageMultiplier(int sources, int maxSources, double perSource, double maxMultiplier) {
        int n = effectiveSources(sources, maxSources);
        double mult = 1.0 + n * Math.max(0.0, perSource);
        return Math.max(1.0, Math.min(mult, maxMultiplier));
    }

    /** Extra ticks of forward flight before the ball turns back (longer range), never negative. */
    public static int extraFlightTicks(int sources, int maxSources, int ticksPerSource) {
        return effectiveSources(sources, maxSources) * Math.max(0, ticksPerSource);
    }
}
