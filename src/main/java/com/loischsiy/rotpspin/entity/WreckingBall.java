package com.loischsiy.rotpspin.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Wrecking Ball (royal guard version of the steel ball): satellite balls hidden inside
 * the main sphere fly out mid-flight and strike from unexpected angles; even a miss
 * raises a shockwave that causes hemispatial neglect. Pure math, no World access.
 */
public final class WreckingBall {

    private WreckingBall() {}

    /** Golden angle: consecutive satellites fan out without ever lining up (docs/spin-lore.md). */
    public static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

    /**
     * Velocity of satellite {@code index} (0-based): the aim direction rotated around Y
     * by golden-angle steps, with alternating slight up/down tilt, so the satellites
     * approach from unexpected angles instead of a single volley.
     */
    public static Vector3d satelliteVelocity(Vector3d aimDir, int index, double speed) {
        Vector3d dir = aimDir.normalize();
        double angle = GOLDEN_ANGLE * index;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = dir.x * cos + dir.z * sin;
        double z = -dir.x * sin + dir.z * cos;
        // Alternate tilt keeps satellites off the exact flight line; index 0 flies true.
        double y = index == 0 ? dir.y : dir.y * 0.5 + (index % 2 == 1 ? 0.25 : -0.25);
        return new Vector3d(x, y, z).normalize().scale(speed);
    }

    /** Velocities for {@code count} satellites; empty list for {@code count <= 0}. */
    public static List<Vector3d> satelliteVelocities(Vector3d aimDir, int count, double speed) {
        List<Vector3d> velocities = new ArrayList<>(Math.max(count, 0));
        for (int i = 0; i < count; i++) {
            velocities.add(satelliteVelocity(aimDir, i, speed));
        }
        return velocities;
    }

    /**
     * Whether {@code toTarget} lies on the victim's left side (horizontal plane only).
     * Facing +Z (south), +X (east) is left: {@code look.z * to.x - look.x * to.z > 0}.
     */
    public static boolean isOnLeftSide(Vector3d look, Vector3d toTarget) {
        return look.z * toTarget.x - look.x * toTarget.z > 0.0;
    }
}
