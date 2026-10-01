package com.loischsiy.rotpspin.entity;

import net.minecraft.util.math.vector.Vector3d;

/**
 * "Rope" woven from a spinning steel ball (SBR ch. 55): the ball anchors in a block and the
 * spin pulls the thrower towards it. Pure math, no World access.
 */
public final class SpinRope {

    private SpinRope() {}

    /** The ball may anchor only within the rope length from its thrower. */
    public static boolean canAnchor(double distanceToOwner, double maxLength) {
        return distanceToOwner <= maxLength;
    }

    /**
     * Owner velocity after one tick of pulling towards the anchor: adds {@code strength} along
     * {@code toAnchor} and caps the result at {@code maxSpeed}.
     */
    public static Vector3d pull(Vector3d velocity, Vector3d toAnchor, double strength, double maxSpeed) {
        double length = toAnchor.length();
        if (length < 1.0E-6) {
            return velocity;
        }
        Vector3d result = velocity.add(toAnchor.scale(strength / length));
        double speed = result.length();
        return speed > maxSpeed ? result.scale(maxSpeed / speed) : result;
    }

    /** The rope lets go when the thrower has arrived or the spin has run out. */
    public static boolean shouldRelease(double distanceToAnchor, int ropeTicks, double releaseDistance, int maxTicks) {
        return distanceToAnchor <= releaseDistance || ropeTicks > maxTicks;
    }
}
