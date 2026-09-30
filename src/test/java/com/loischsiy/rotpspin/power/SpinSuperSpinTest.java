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
            t = SpinSuperSpin.nextGallopTicks(t, true, 0, 20, false, true);
        }
        assertEquals(40, t);
        assertTrue(SpinSuperSpin.isGallopReady(t, 40));
        assertFalse(SpinSuperSpin.isGallopReady(39, 40));
    }

    @Test
    void realDisturbanceBreaksCompletely() {
        assertEquals(0, SpinSuperSpin.nextGallopTicks(500, true, 0, 20, true, true));
        assertEquals(0, SpinSuperSpin.nextGallopTicks(500, true, 0, 20, false, false));
        assertEquals(0, SpinSuperSpin.nextGallopTicks(500, false, 21, 20, false, true));
    }

    @Test
    void briefSlowDownKeepsBuildUp() {
        int t = 100;
        int slow = 0;
        for (int i = 0; i < 20; i++) {
            slow = SpinSuperSpin.nextSlowTicks(slow, false);
            t = SpinSuperSpin.nextGallopTicks(t, false, slow, 20, false, true);
        }
        assertEquals(100, t, "a hill or a turn holds the build-up");
        slow = SpinSuperSpin.nextSlowTicks(slow, true);
        assertEquals(0, slow);
        assertEquals(101, SpinSuperSpin.nextGallopTicks(t, true, slow, 20, false, true));
    }

    @Test
    void longStopBreaks() {
        int t = 100;
        int slow = 0;
        for (int i = 0; i < 21; i++) {
            slow = SpinSuperSpin.nextSlowTicks(slow, false);
            t = SpinSuperSpin.nextGallopTicks(t, false, slow, 20, false, true);
        }
        assertEquals(0, t);
    }

    @Test
    void zeroGraceIsStrict() {
        int slow = SpinSuperSpin.nextSlowTicks(0, false);
        assertEquals(0, SpinSuperSpin.nextGallopTicks(100, false, slow, 0, false, true));
    }

    @Test
    void crashNeedsCollisionAndRealStop() {
        assertTrue(SpinSuperSpin.isCrash(true, 0.05, 0.3, 0.35));
        assertFalse(SpinSuperSpin.isCrash(true, 0.25, 0.3, 0.35), "sliding along a wall");
        assertFalse(SpinSuperSpin.isCrash(false, 0.0, 0.3, 0.35), "a lagging packet");
    }

    @Test
    void smoothingRidesThroughLagTick() {
        double s = 0.3;
        s = SpinSuperSpin.smoothSpeed(s, 0.0);
        assertTrue(s >= 0.2, "one empty packet tick does not drop below gallop speed: " + s);
        s = SpinSuperSpin.smoothSpeed(s, 0.6);
        assertTrue(s > 0.25 && s < 0.35, "catch-up tick restores speed: " + s);
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
