package com.loischsiy.rotpspin.power;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Cloak held as a sail by a spinning ball (SBR ch. 11): the spin keeps the cloth taut, the fall
 * slows down and turns into a glide along the look direction. Pure math, no World access.
 */
public final class SpinSail {

    private SpinSail() {}

    /** A fall starts the sail only after this many blocks, so an ordinary jump costs nothing. */
    public static boolean shouldStart(boolean sailing, float fallDistance, double minFallDistance) {
        return sailing || fallDistance >= minFallDistance;
    }

    /**
     * Energy gate with hysteresis: keeping the sail open needs one tick's cost, opening it again
     * needs {@code startEnergy}, so an exhausted user drops instead of flickering open and shut.
     */
    public static boolean hasEnergy(boolean sailing, double energy, double costPerTick, double startEnergy) {
        return energy >= (sailing ? costPerTick : Math.max(costPerTick, startEnergy));
    }

    /**
     * Velocity under the sail: the descent is capped at {@code maxFallSpeed}, the horizontal look
     * direction gets {@code push} per tick, horizontal speed is capped at {@code maxHorizontalSpeed}.
     */
    public static Vector3d sail(Vector3d velocity, Vector3d look, double maxFallSpeed, double push,
            double maxHorizontalSpeed) {
        double y = Math.max(velocity.y, -maxFallSpeed);
        double x = velocity.x;
        double z = velocity.z;
        double lookLength = Math.sqrt(look.x * look.x + look.z * look.z);
        if (lookLength > 1.0E-6) {
            x += look.x / lookLength * push;
            z += look.z / lookLength * push;
        }
        double horizontal = Math.sqrt(x * x + z * z);
        if (horizontal > maxHorizontalSpeed) {
            double scale = maxHorizontalSpeed / horizontal;
            x *= scale;
            z *= scale;
        }
        return new Vector3d(x, y, z);
    }
}
