package com.loischsiy.rotpspin.entity;

import net.minecraft.util.math.vector.Vector3d;

/** Ricochet of a spinning ball off a block face. Pure math, no World access. */
public final class SpinRicochet {

    private SpinRicochet() {}

    /**
     * Mirror reflection of {@code velocity} off a surface with the unit {@code normal},
     * keeping {@code retention} (0..1) of the speed.
     */
    public static Vector3d reflect(Vector3d velocity, Vector3d normal, double retention) {
        double dot = velocity.dot(normal);
        return velocity.subtract(normal.scale(2 * dot)).scale(retention);
    }

    /** Whether the ball may bounce once more instead of stopping. */
    public static boolean canBounce(int bouncesDone, int maxBounces, double speedAfter, double minSpeed) {
        return bouncesDone < maxBounces && speedAfter >= minSpeed;
    }
}
