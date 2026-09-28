package com.loischsiy.rotpspin.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.vector.Vector3d;

class WreckingBallTest {
    private static final double EPS = 1e-6;

    @Test
    void firstSatelliteFliesTrue() {
        Vector3d aim = new Vector3d(0, 0, 1);
        Vector3d out = WreckingBall.satelliteVelocity(aim, 0, 1.2);
        assertEquals(0, out.x, EPS);
        assertEquals(0, out.y, EPS);
        assertEquals(1.2, out.z, EPS);
    }

    @Test
    void satellitesKeepSpeedAndSpreadOut() {
        Vector3d aim = new Vector3d(1, 0, 0);
        List<Vector3d> velocities = WreckingBall.satelliteVelocities(aim, 5, 1.5);
        assertEquals(5, velocities.size());
        for (Vector3d v : velocities) {
            assertEquals(1.5, v.length(), EPS);
        }
        // Golden-angle fan: no two satellites share a direction.
        for (int i = 0; i < velocities.size(); i++) {
            for (int j = i + 1; j < velocities.size(); j++) {
                assertTrue(velocities.get(i).distanceTo(velocities.get(j)) > 0.1);
            }
        }
    }

    @Test
    void noSatellitesForNonPositiveCount() {
        assertTrue(WreckingBall.satelliteVelocities(new Vector3d(0, 0, 1), 0, 1.0).isEmpty());
    }

    @Test
    void leftSideFacingSouth() {
        Vector3d south = new Vector3d(0, 0, 1);
        // Facing south, east (+X) is on the left.
        assertTrue(WreckingBall.isOnLeftSide(south, new Vector3d(1, 0, 0)));
        assertFalse(WreckingBall.isOnLeftSide(south, new Vector3d(-1, 0, 0)));
        assertFalse(WreckingBall.isOnLeftSide(south, new Vector3d(0, 0, 1)));
        assertFalse(WreckingBall.isOnLeftSide(south, new Vector3d(0, 0, -1)));
    }

    @Test
    void leftSideFacingNorth() {
        Vector3d north = new Vector3d(0, 0, -1);
        // Facing north, west (-X) is on the left.
        assertTrue(WreckingBall.isOnLeftSide(north, new Vector3d(-1, 0, 0)));
        assertFalse(WreckingBall.isOnLeftSide(north, new Vector3d(1, 0, 0)));
    }

    @Test
    void diagonalLookStillSplitsHalves() {
        Vector3d look = new Vector3d(1, 0, 1).normalize();
        // Directly ahead and behind are never "left".
        assertFalse(WreckingBall.isOnLeftSide(look, new Vector3d(1, 0, 1)));
        assertFalse(WreckingBall.isOnLeftSide(look, new Vector3d(-1, 0, -1)));
        // The two sides disagree.
        boolean a = WreckingBall.isOnLeftSide(look, new Vector3d(-1, 0, 1));
        boolean b = WreckingBall.isOnLeftSide(look, new Vector3d(1, 0, -1));
        assertTrue(a != b);
    }
}
