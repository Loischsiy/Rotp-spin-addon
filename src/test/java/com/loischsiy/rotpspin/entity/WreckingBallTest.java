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

    @Test
    void canonHasFourteenSatellites() {
        assertEquals(14, WreckingBall.SATELLITE_COUNT);
    }

    @Test
    void socketsAreUnitVectorsSpreadEvenly() {
        List<Vector3d> sockets = WreckingBall.socketDirections(WreckingBall.SATELLITE_COUNT);
        assertEquals(14, sockets.size());
        for (Vector3d s : sockets) {
            assertEquals(1.0, s.length(), EPS);
        }
        // Satellites (radius 0.055) sit on a 0.19 sphere: neighbours must not overlap.
        double minChord = Double.MAX_VALUE;
        for (int i = 0; i < sockets.size(); i++) {
            for (int j = i + 1; j < sockets.size(); j++) {
                minChord = Math.min(minChord, sockets.get(i).distanceTo(sockets.get(j)) * 0.19);
            }
        }
        assertTrue(minChord > 2 * 0.055, "sockets overlap: " + minChord);
        // Even coverage: the lattice is balanced around the centre.
        Vector3d sum = Vector3d.ZERO;
        for (Vector3d s : sockets) {
            sum = sum.add(s);
        }
        assertTrue(sum.length() < 1.5);
    }

    @Test
    void noSocketsForNonPositiveCount() {
        assertTrue(WreckingBall.socketDirections(0).isEmpty());
    }
}
