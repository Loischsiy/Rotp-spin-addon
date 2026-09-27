package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpinLeapTest {
    private static final float BASE = 1.3F;
    private static final double BASE_SPEED = 0.1;
    private static final float EPS = 1e-5F;

    // What RotP does afterwards in NonStandPower#leapStrength
    private static float afterRotp(float strength, double speed) {
        return (float) (strength * speed / BASE_SPEED);
    }

    @Test
    void normalSpeedKeepsBase() {
        assertEquals(BASE, SpinLeap.strength(BASE, BASE_SPEED, BASE_SPEED, true), EPS);
    }

    @Test
    void slownessIsCompensatedAfterRotpMultiplier() {
        double slowed = BASE_SPEED * 0.4; // Slowness III
        assertEquals(BASE, afterRotp(SpinLeap.strength(BASE, slowed, BASE_SPEED, true), slowed), EPS);
    }

    @Test
    void speedBoostStillHelps() {
        double fast = BASE_SPEED * 1.4;
        float strength = SpinLeap.strength(BASE, fast, BASE_SPEED, true);
        assertEquals(BASE, strength, EPS);
        assertEquals(BASE * 1.4F, afterRotp(strength, fast), EPS);
    }

    @Test
    void compensationCanBeDisabled() {
        double slowed = BASE_SPEED * 0.4;
        assertEquals(BASE, SpinLeap.strength(BASE, slowed, BASE_SPEED, false), EPS);
    }

    @Test
    void immobileUserDoesNotExplode() {
        float strength = SpinLeap.strength(BASE, 0, BASE_SPEED, true);
        assertEquals(BASE / SpinLeap.MIN_SPEED_RATIO, strength, 1e-3F);
    }

    @Test
    void zeroBaseSpeedIsSafe() {
        assertEquals(BASE, SpinLeap.strength(BASE, 0, 0, true), EPS);
    }
}
