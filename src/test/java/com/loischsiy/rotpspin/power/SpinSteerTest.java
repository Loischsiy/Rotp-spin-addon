package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.vector.Vector3d;

class SpinSteerTest {
    // Vector3d#normalize in 1.16.5 uses MathHelper.sqrt (float precision, ~1e-7 error).
    private static final double EPS = 1.0E-6;

    @Test
    void zeroTurnRateKeepsDirection() {
        Vector3d v = SpinSteer.steer(new Vector3d(2, 0, 0), new Vector3d(0, 0, 1), 0, 0);
        assertEquals(2, v.x, EPS);
        assertEquals(0, v.z, EPS);
    }

    @Test
    void fullTurnRateSnapsToDesiredKeepingSpeed() {
        Vector3d v = SpinSteer.steer(new Vector3d(2, 0, 0), new Vector3d(0, 0, 5), 1, 0);
        assertEquals(0, v.x, EPS);
        assertEquals(2, v.z, EPS);
    }

    @Test
    void partialTurnRotatesTowardsDesired() {
        Vector3d v = SpinSteer.steer(new Vector3d(1, 0, 0), new Vector3d(0, 0, 1), 0.5, 0);
        assertEquals(1, v.length(), EPS);
        assertEquals(v.x, v.z, EPS); // 45 degrees
    }

    @Test
    void minSpeedIsEnforced() {
        Vector3d v = SpinSteer.steer(new Vector3d(0.1, 0, 0), new Vector3d(1, 0, 0), 0.25, 1.2);
        assertEquals(1.2, v.length(), EPS);
    }

    @Test
    void zeroVelocityTakesDesiredDirection() {
        Vector3d v = SpinSteer.steer(Vector3d.ZERO, new Vector3d(0, 3, 0), 0.1, 1);
        assertEquals(1, v.y, EPS);
    }

    @Test
    void oppositeDirectionDoesNotProduceNaN() {
        Vector3d v = SpinSteer.steer(new Vector3d(1, 0, 0), new Vector3d(-1, 0, 0), 0.5, 0);
        assertTrue(Double.isFinite(v.x));
        assertEquals(-1, v.x, EPS);
    }

    @Test
    void zeroDesiredDirectionLeavesVelocity() {
        Vector3d in = new Vector3d(1, 2, 3);
        assertEquals(in, SpinSteer.steer(in, Vector3d.ZERO, 0.5, 5));
    }
}
