package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpinResonanceTest {
    private static final double EPS = 1e-9;

    @Test
    void noSourcesNoBonus() {
        assertEquals(1.0, SpinResonance.damageMultiplier(0, 3, 0.15, 1.5), EPS);
        assertEquals(0, SpinResonance.extraFlightTicks(0, 3, 5));
    }

    @Test
    void growsWithSources() {
        assertEquals(1.15, SpinResonance.damageMultiplier(1, 3, 0.15, 1.5), EPS);
        assertEquals(1.30, SpinResonance.damageMultiplier(2, 3, 0.15, 1.5), EPS);
        assertEquals(10, SpinResonance.extraFlightTicks(2, 3, 5));
    }

    @Test
    void cappedBySourcesAndMultiplier() {
        assertEquals(1.45, SpinResonance.damageMultiplier(10, 3, 0.15, 1.5), EPS);
        assertEquals(1.5, SpinResonance.damageMultiplier(10, 10, 0.15, 1.5), EPS);
        assertEquals(15, SpinResonance.extraFlightTicks(10, 3, 5));
    }

    @Test
    void neverNegative() {
        assertEquals(1.0, SpinResonance.damageMultiplier(-2, 3, 0.15, 1.5), EPS);
        assertEquals(1.0, SpinResonance.damageMultiplier(2, 3, -0.5, 1.5), EPS);
        assertEquals(0, SpinResonance.extraFlightTicks(2, 3, -5));
        assertEquals(0, SpinResonance.extraFlightTicks(2, -1, 5));
    }
}
