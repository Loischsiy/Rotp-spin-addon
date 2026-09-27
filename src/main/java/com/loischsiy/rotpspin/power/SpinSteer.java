package com.loischsiy.rotpspin.power;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Steering of a spinning steel ball in flight: the rotation axis is re-aimed a little every tick.
 * Pure math, no World access.
 */
public final class SpinSteer {
    private static final double EPSILON_SQ = 1.0E-8;

    private SpinSteer() {}

    /**
     * Turns {@code velocity} towards {@code desiredDirection} by {@code turnRate} (0..1, linear blend
     * of the unit directions) and keeps at least {@code minSpeed}.
     * A zero velocity or a direction exactly opposite to the desired one snaps to the desired direction.
     */
    public static Vector3d steer(Vector3d velocity, Vector3d desiredDirection, double turnRate, double minSpeed) {
        if (desiredDirection.lengthSqr() < EPSILON_SQ) {
            return velocity;
        }
        Vector3d desired = desiredDirection.normalize();
        double speed = Math.max(velocity.length(), minSpeed);
        double rate = Math.max(0, Math.min(1, turnRate));
        Vector3d current = velocity.lengthSqr() < EPSILON_SQ ? desired : velocity.normalize();
        Vector3d blended = current.scale(1 - rate).add(desired.scale(rate));
        if (blended.lengthSqr() < EPSILON_SQ) {
            blended = desired;
        }
        return blended.normalize().scale(speed);
    }
}
