package com.loischsiy.rotpspin.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.vector.Vector3d;

class SpinRicochetTest {
    private static final double EPS = 1e-9;

    @Test
    void floorFlipsVerticalOnly() {
        Vector3d out = SpinRicochet.reflect(new Vector3d(1, -2, 0.5), new Vector3d(0, 1, 0), 1.0);
        assertEquals(1, out.x, EPS);
        assertEquals(2, out.y, EPS);
        assertEquals(0.5, out.z, EPS);
    }

    @Test
    void wallFlipsHorizontalAndKeepsRetention() {
        Vector3d out = SpinRicochet.reflect(new Vector3d(2, 0.4, 0), new Vector3d(-1, 0, 0), 0.5);
        assertEquals(-1, out.x, EPS);
        assertEquals(0.2, out.y, EPS);
        assertEquals(0, out.z, EPS);
    }

    @Test
    void speedScalesByRetention() {
        Vector3d in = new Vector3d(0.3, -1.1, 0.7);
        Vector3d out = SpinRicochet.reflect(in, new Vector3d(0, 1, 0), 0.7);
        assertEquals(in.length() * 0.7, out.length(), EPS);
    }

    @Test
    void bounceLimits() {
        assertTrue(SpinRicochet.canBounce(0, 2, 1.0, 0.4));
        assertTrue(SpinRicochet.canBounce(1, 2, 0.4, 0.4));
        assertFalse(SpinRicochet.canBounce(2, 2, 1.0, 0.4));
        assertFalse(SpinRicochet.canBounce(0, 2, 0.39, 0.4));
        assertFalse(SpinRicochet.canBounce(0, 0, 1.0, 0.0));
    }
}
