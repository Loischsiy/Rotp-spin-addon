package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpinChargeTest {
    private static final double EPS = 1e-9;

    @Test
    void fractionGrowsAndClamps() {
        assertEquals(0.0, SpinCharge.fraction(0, 40), EPS);
        assertEquals(0.5, SpinCharge.fraction(20, 40), EPS);
        assertEquals(1.0, SpinCharge.fraction(80, 40), EPS);
        assertEquals(0.0, SpinCharge.fraction(-5, 40), EPS);
    }

    @Test
    void zeroMaxTicksMeansInstantFullCharge() {
        assertEquals(1.0, SpinCharge.fraction(0, 0), EPS);
        assertTrue(SpinCharge.isFull(0, 0, false, 0.5));
    }

    @Test
    void chippedBallIsCapped() {
        assertEquals(0.5, SpinCharge.cap(1.0, true, 0.5), EPS);
        assertEquals(0.3, SpinCharge.cap(0.3, true, 0.5), EPS);
        assertEquals(1.0, SpinCharge.cap(1.0, false, 0.5), EPS);
        assertEquals(1.0, SpinCharge.cap(1.0, true, 2.0), EPS);
        assertEquals(0.0, SpinCharge.cap(1.0, true, -1.0), EPS);
    }

    @Test
    void chippedBallIsFullEarlier() {
        assertFalse(SpinCharge.isFull(19, 40, true, 0.5));
        assertTrue(SpinCharge.isFull(20, 40, true, 0.5));
        assertFalse(SpinCharge.isFull(20, 40, false, 0.5));
        assertTrue(SpinCharge.isFull(40, 40, false, 0.5));
    }

    @Test
    void multiplierIsLinearAndNeverBelowOne() {
        assertEquals(1.0, SpinCharge.multiplier(0.0, 1.8), EPS);
        assertEquals(1.4, SpinCharge.multiplier(0.5, 1.8), EPS);
        assertEquals(1.8, SpinCharge.multiplier(1.0, 1.8), EPS);
        assertEquals(1.0, SpinCharge.multiplier(1.0, 0.5), EPS);
    }

    @Test
    void percentRounds() {
        assertEquals(0, SpinCharge.percent(0.0));
        assertEquals(50, SpinCharge.percent(0.5));
        assertEquals(100, SpinCharge.percent(1.5));
    }
}
