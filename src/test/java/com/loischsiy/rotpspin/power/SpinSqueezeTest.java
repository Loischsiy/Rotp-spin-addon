package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.loischsiy.rotpspin.power.SpinSqueeze.Zone;

class SpinSqueezeTest {
    private static final double EPS = 1e-9;

    @Test
    void relativeHeightClamped() {
        assertEquals(0.5, SpinSqueeze.relativeHeight(65.0, 64.0, 2.0), EPS);
        assertEquals(0.0, SpinSqueeze.relativeHeight(63.0, 64.0, 2.0), EPS);
        assertEquals(1.0, SpinSqueeze.relativeHeight(70.0, 64.0, 2.0), EPS);
        assertEquals(0.5, SpinSqueeze.relativeHeight(70.0, 64.0, 0.0), EPS);
    }

    @Test
    void zoneByHeight() {
        assertEquals(Zone.LEGS, SpinSqueeze.zone(0.2, 0.5, 0.85));
        assertEquals(Zone.BODY, SpinSqueeze.zone(0.5, 0.5, 0.85));
        assertEquals(Zone.BODY, SpinSqueeze.zone(0.84, 0.5, 0.85));
        assertEquals(Zone.HEAD, SpinSqueeze.zone(0.85, 0.5, 0.85));
        assertEquals(Zone.HEAD, SpinSqueeze.zone(1.0, 0.5, 0.85));
    }

    @Test
    void misorderedThresholdsHaveNoBody() {
        assertEquals(Zone.LEGS, SpinSqueeze.zone(0.5, 0.6, 0.4));
        assertEquals(Zone.HEAD, SpinSqueeze.zone(0.7, 0.6, 0.4));
    }

    @Test
    void durationScaledWhenChipped() {
        assertEquals(60, SpinSqueeze.duration(60, false, 0.5));
        assertEquals(30, SpinSqueeze.duration(60, true, 0.5));
        assertEquals(0, SpinSqueeze.duration(-5, false, 0.5));
        assertEquals(0, SpinSqueeze.duration(60, true, -1.0));
    }

    @Test
    void gatedByToggleAndLesson() {
        assertTrue(SpinSqueeze.applies(true, 0, 0));
        assertTrue(SpinSqueeze.applies(true, 3, 2));
        assertFalse(SpinSqueeze.applies(true, 1, 2));
        assertFalse(SpinSqueeze.applies(false, 5, 0));
    }
}
