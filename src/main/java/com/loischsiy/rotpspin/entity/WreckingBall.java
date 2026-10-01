package com.loischsiy.rotpspin.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Wrecking Ball (royal guard version of the steel ball): 14 satellite balls are set into
 * the main sphere; mid-flight they fly out and strike from unexpected angles, and even a miss
 * raises a shockwave that causes hemispatial neglect. Pure math, no World access.
 */
public final class WreckingBall {

    private WreckingBall() {}

    /** Canon: the sphere carries 14 small parts called satellites (docs/spin-lore.md). */
    public static final int SATELLITE_COUNT = 14;

    /** Golden angle: consecutive satellites fan out without ever lining up (docs/spin-lore.md). */
    public static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

    /**
     * Unit directions from the sphere centre to the sockets of {@code count} satellites: an even
     * Fibonacci lattice on the sphere (golden-angle steps), the same spacing rule as the flight fan.
     * Used by the renderer to seat the satellites; empty list for {@code count <= 0}.
     */
    public static List<Vector3d> socketDirections(int count) {
        List<Vector3d> directions = new ArrayList<>(Math.max(count, 0));
        for (int i = 0; i < count; i++) {
            double y = 1.0 - 2.0 * (i + 0.5) / count;
            double ring = Math.sqrt(1.0 - y * y);
            double angle = GOLDEN_ANGLE * i;
            directions.add(new Vector3d(ring * Math.cos(angle), y, ring * Math.sin(angle)));
        }
        return directions;
    }

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

    /**
     * Opacity of the dark veil over the victim's left half: {@code maxOpacity} while the neglect
     * holds, fading linearly to 0 over the last {@code fadeOutTicks} ticks. 0 when nothing is left.
     */
    public static float neglectVeilOpacity(int remainingTicks, int fadeOutTicks, double maxOpacity) {
        if (remainingTicks <= 0 || maxOpacity <= 0.0) {
            return 0.0F;
        }
        double max = Math.min(maxOpacity, 1.0);
        if (fadeOutTicks <= 0 || remainingTicks >= fadeOutTicks) {
            return (float) max;
        }
        return (float) (max * remainingTicks / fadeOutTicks);
    }

    /**
     * Yaw after one tick of drifting to the right (Minecraft yaw grows clockwise seen from above,
     * so facing south a growing yaw turns west, which is the right hand). Not wrapped: the camera
     * interpolates from the previous yaw, a jump of 360 would spin it. Negative rates count as 0.
     */
    public static float veerRight(float yaw, double degreesPerTick) {
        return (float) (yaw + Math.max(degreesPerTick, 0.0));
    }
}
