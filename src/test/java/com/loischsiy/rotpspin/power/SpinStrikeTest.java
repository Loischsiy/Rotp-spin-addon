package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpinStrikeTest {
    private static final double EPS = 1e-9;

    @Test
    void perfectBallKeepsFullStrike() {
        assertEquals(1.0, SpinStrike.efficiency(false, 0.3), EPS);
        assertEquals(6.0, SpinStrike.scaled(6.0, false, 0.3), EPS);
    }

    @Test
    void chippedBallIsWeaker() {
        assertEquals(0.5, SpinStrike.efficiency(true, 0.5), EPS);
        assertEquals(1.5, SpinStrike.scaled(3.0, true, 0.5), EPS);
    }

    @Test
    void chippedBallNeverStrongerThanPerfect() {
        assertEquals(1.0, SpinStrike.efficiency(true, 2.0), EPS);
        assertEquals(0.0, SpinStrike.efficiency(true, -1.0), EPS);
    }

    @Test
    void neverNegative() {
        assertEquals(0.0, SpinStrike.scaled(-4.0, false, 0.5), EPS);
    }

    @Test
    void liftOnlyRaises() {
        assertEquals(0.4, SpinStrike.liftedY(0.1, 0.4), EPS);
        assertEquals(0.8, SpinStrike.liftedY(0.8, 0.4), EPS);
        assertEquals(0.0, SpinStrike.liftedY(-0.5, 0.0), EPS);
    }
}
