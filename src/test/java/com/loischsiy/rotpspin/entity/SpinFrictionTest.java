package com.loischsiy.rotpspin.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpinFrictionTest {
    private static final double EPS = 1e-9;

    @Test
    void stripsOnlyWhenSpinningOutwardAndFast() {
        assertTrue(SpinFriction.canStripBark(true, true, false, 1.0, 0.8));
        assertTrue(SpinFriction.canStripBark(true, true, false, 0.8, 0.8));
        assertFalse(SpinFriction.canStripBark(true, true, false, 0.5, 0.8));
        assertFalse(SpinFriction.canStripBark(true, false, false, 2.0, 0.8));
        assertFalse(SpinFriction.canStripBark(true, true, true, 2.0, 0.8));
        assertFalse(SpinFriction.canStripBark(false, true, false, 2.0, 0.8));
    }

    @Test
    void frictionEatsSpeed() {
        assertEquals(0.6, SpinFriction.speedAfterStrip(1.0, 0.6), EPS);
        assertEquals(1.0, SpinFriction.speedAfterStrip(1.0, 2.0), EPS);
        assertEquals(0.0, SpinFriction.speedAfterStrip(1.0, -1.0), EPS);
    }
}
