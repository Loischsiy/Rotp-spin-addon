package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.vector.Vector3d;

class SpinSailTest {
    private static final double EPS = 1e-6;

    @Test
    void startsAfterMinFallAndKeepsGoing() {
        assertFalse(SpinSail.shouldStart(false, 1.0F, 1.5));
        assertTrue(SpinSail.shouldStart(false, 1.5F, 1.5));
        assertTrue(SpinSail.shouldStart(true, 0.0F, 1.5));
    }

    @Test
    void capsDescentOnly() {
        Vector3d out = SpinSail.sail(new Vector3d(0, -1.0, 0), new Vector3d(0, -1, 0), 0.12, 0.0, 0.5);
        assertEquals(-0.12, out.y, EPS);
        Vector3d up = SpinSail.sail(new Vector3d(0, 0.3, 0), new Vector3d(0, 0, 1), 0.12, 0.0, 0.5);
        assertEquals(0.3, up.y, EPS);
    }

    @Test
    void pushesAlongHorizontalLook() {
        Vector3d out = SpinSail.sail(Vector3d.ZERO, new Vector3d(0, -0.8, 0.6), 0.12, 0.03, 0.5);
        assertEquals(0, out.x, EPS);
        assertEquals(0.03, out.z, EPS);
    }

    @Test
    void capsHorizontalSpeed() {
        Vector3d out = SpinSail.sail(new Vector3d(0.6, -0.5, 0), new Vector3d(1, 0, 0), 0.12, 0.03, 0.5);
        assertEquals(0.5, Math.sqrt(out.x * out.x + out.z * out.z), EPS);
        assertEquals(-0.12, out.y, EPS);
    }

    @Test
    void reopeningNeedsStartEnergy() {
        assertTrue(SpinSail.hasEnergy(true, 0.75, 0.75, 10.0));
        assertFalse(SpinSail.hasEnergy(false, 2.75, 0.75, 10.0));
        assertTrue(SpinSail.hasEnergy(false, 10.0, 0.75, 10.0));
        assertFalse(SpinSail.hasEnergy(false, 0.5, 0.75, 0.0));
    }
}
