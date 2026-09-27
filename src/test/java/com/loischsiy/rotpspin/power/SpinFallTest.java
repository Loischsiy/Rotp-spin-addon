package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpinFallTest {
    private static final float SPIN_LEAP = 1.3F;
    private static final float EPS = 1e-4F;

    // What RotP's GameplayEventHandler#onLivingFall leaves
    private static float afterRotp(float original, float leap) {
        return original > 3 ? Math.max(original - (leap + 5) * 3, 0) : original;
    }

    @Test
    void rotpBonusIsRemovedForSpin() {
        float original = 15;
        float result = SpinFall.correct(original, afterRotp(original, SPIN_LEAP), SPIN_LEAP, 0, 0);
        assertEquals(original, result, EPS);
    }

    @Test
    void configuredReductionApplies() {
        float original = 15;
        float result = SpinFall.correct(original, afterRotp(original, SPIN_LEAP), SPIN_LEAP, 0, 4);
        assertEquals(11, result, EPS);
    }

    @Test
    void shortFallIsUntouched() {
        assertEquals(2.5F, SpinFall.correct(2.5F, 2.5F, SPIN_LEAP, 0, 0), EPS);
    }

    @Test
    void noLeapNoChange() {
        assertEquals(10, SpinFall.correct(10, 10, 0, 0, 0), EPS);
    }

    @Test
    void leapingStandKeepsItsBonus() {
        float standLeap = 2.0F; // stronger than Spin, RotP used it
        float original = 40;
        float current = afterRotp(original, standLeap);
        assertEquals(current, SpinFall.correct(original, current, SPIN_LEAP, standLeap, 0), EPS);
    }

    @Test
    void weakerStandLeapStillCountsAsItsOwnBonus() {
        float standLeap = 0.5F; // RotP used the Spin leap (1.3); the Stand alone would give (0.5 + 5) * 3 = 16.5
        float original = 30;
        float result = SpinFall.correct(original, afterRotp(original, SPIN_LEAP), SPIN_LEAP, standLeap, 0);
        assertEquals(30 - 16.5F, result, EPS);
    }

    @Test
    void otherHandlersChangesAreKept() {
        float original = 20;
        float otherMod = 2; // some other handler removed 2 more blocks after RotP
        float current = Math.max(afterRotp(original, SPIN_LEAP) - otherMod, 0);
        // afterRotp is 1.1, so the other mod could only take 1.1: result = 20 - 1.1
        assertEquals(original - 1.1F, SpinFall.correct(original, current, SPIN_LEAP, 0, 0), EPS);
    }
}
