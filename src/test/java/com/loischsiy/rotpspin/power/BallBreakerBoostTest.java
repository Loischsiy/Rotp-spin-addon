package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BallBreakerBoostTest {
    private static final double EPS = 1e-9;

    @Test
    void needsBothBreakerAndGolden() {
        assertFalse(BallBreakerBoost.shouldBoost(false, 2.0));
        assertFalse(BallBreakerBoost.shouldBoost(true, 1.0));
        assertFalse(BallBreakerBoost.shouldBoost(false, 1.0));
        assertTrue(BallBreakerBoost.shouldBoost(true, 1.5));
        assertTrue(BallBreakerBoost.shouldBoost(true, 2.0));
    }

    @Test
    void multipliesOnlyGoldenThrows() {
        assertEquals(3.0, BallBreakerBoost.boostedMultiplier(2.0, true, 1.5), EPS);
        assertEquals(2.25, BallBreakerBoost.boostedMultiplier(1.5, true, 1.5), EPS);
        assertEquals(2.0, BallBreakerBoost.boostedMultiplier(2.0, false, 1.5), EPS);
        assertEquals(1.0, BallBreakerBoost.boostedMultiplier(1.0, true, 1.5), EPS);
    }
}
