package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpinSuperSpinTest {
    private static final double EPS = 1e-9;

    @Test
    void gallopBuildsUpTickByTick() {
        int t = 0;
        for (int i = 0; i < 40; i++) {
            t = SpinSuperSpin.nextGallopTicks(t, true, false, true);
        }
        assertEquals(40, t);
        assertTrue(SpinSuperSpin.isGallopReady(t, 40));
        assertFalse(SpinSuperSpin.isGallopReady(39, 40));
    }

    @Test
    void anyDisturbanceBreaksCompletely() {
        assertEquals(0, SpinSuperSpin.nextGallopTicks(500, true, true, true));
        assertEquals(0, SpinSuperSpin.nextGallopTicks(500, false, false, true));
        assertEquals(0, SpinSuperSpin.nextGallopTicks(500, true, false, false));
    }

    @Test
    void zeroRequirementStillNeedsOneTick() {
        assertFalse(SpinSuperSpin.isGallopReady(0, 0));
        assertTrue(SpinSuperSpin.isGallopReady(1, 0));
    }

    @Test
    void healthyHorse() {
        assertTrue(SpinSuperSpin.isHealthy(20F, 20F, 0.5));
        assertTrue(SpinSuperSpin.isHealthy(10F, 20F, 0.5));
        assertFalse(SpinSuperSpin.isHealthy(9F, 20F, 0.5));
        assertFalse(SpinSuperSpin.isHealthy(0F, 0F, 0.0));
    }

    @Test
    void detourConditions() {
        assertTrue(SpinSuperSpin.canDetour(5, false, true, true, 9.0, 4.0));
        assertFalse(SpinSuperSpin.canDetour(4, false, true, true, 9.0, 4.0));
        assertFalse(SpinSuperSpin.canDetour(5, true, true, true, 9.0, 4.0));
        assertFalse(SpinSuperSpin.canDetour(5, false, false, true, 9.0, 4.0));
        assertFalse(SpinSuperSpin.canDetour(5, false, true, false, 9.0, 4.0));
        assertFalse(SpinSuperSpin.canDetour(5, false, true, true, 17.0, 4.0));
    }

    @Test
    void detourDuration() {
        assertEquals(1100L, SpinSuperSpin.detourUntil(1000L, 100));
        assertEquals(1000L, SpinSuperSpin.detourUntil(1000L, -1));
    }

    @Test
    void multiplierNeverLowers() {
        assertEquals(1.5, SpinSuperSpin.multiplier(false, 1.5, 3.0), EPS);
        assertEquals(3.0, SpinSuperSpin.multiplier(true, 1.5, 3.0), EPS);
        assertEquals(4.0, SpinSuperSpin.multiplier(true, 4.0, 3.0), EPS);
    }
}
