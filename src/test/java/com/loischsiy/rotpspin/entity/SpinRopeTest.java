package com.loischsiy.rotpspin.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.vector.Vector3d;

class SpinRopeTest {
    private static final double EPS = 1e-6;

    @Test
    void anchorsOnlyWithinRopeLength() {
        assertTrue(SpinRope.canAnchor(10, 32));
        assertTrue(SpinRope.canAnchor(32, 32));
        assertFalse(SpinRope.canAnchor(32.5, 32));
    }

    @Test
    void pullAddsStrengthTowardsAnchor() {
        Vector3d out = SpinRope.pull(Vector3d.ZERO, new Vector3d(0, 0, 10), 0.25, 1.2);
        assertEquals(0, out.x, EPS);
        assertEquals(0, out.y, EPS);
        assertEquals(0.25, out.z, EPS);
    }

    @Test
    void pullIsCappedAtMaxSpeed() {
        Vector3d out = SpinRope.pull(new Vector3d(0, 0, 1.1), new Vector3d(0, 0, 10), 0.25, 1.2);
        assertEquals(1.2, out.length(), EPS);
    }

    @Test
    void pullAtAnchorKeepsVelocity() {
        Vector3d v = new Vector3d(0.1, -0.2, 0.3);
        Vector3d out = SpinRope.pull(v, Vector3d.ZERO, 0.25, 1.2);
        assertEquals(v.x, out.x, EPS);
        assertEquals(v.y, out.y, EPS);
        assertEquals(v.z, out.z, EPS);
    }

    @Test
    void releasesOnArrivalOrTimeout() {
        assertTrue(SpinRope.shouldRelease(1.5, 10, 2.0, 60));
        assertTrue(SpinRope.shouldRelease(10, 61, 2.0, 60));
        assertFalse(SpinRope.shouldRelease(10, 60, 2.0, 60));
    }
}
