package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BallBreakerAgingTest {

    @Test
    void everyTouchDeepensAgingUpToTheCap() {
        assertEquals(0, BallBreakerAging.nextAmplifier(-1, 3), "first touch");
        assertEquals(1, BallBreakerAging.nextAmplifier(0, 3));
        assertEquals(2, BallBreakerAging.nextAmplifier(1, 3));
        assertEquals(2, BallBreakerAging.nextAmplifier(2, 3), "capped");
        assertEquals(0, BallBreakerAging.nextAmplifier(5, 1), "one level only");
    }

    @Test
    void standStaminaDrainScalesWithLevelAndChippedBall() {
        assertEquals(60f, BallBreakerAging.standStaminaDrain(60, 1, 1.0), 1e-6);
        assertEquals(180f, BallBreakerAging.standStaminaDrain(60, 3, 1.0), 1e-6);
        assertEquals(90f, BallBreakerAging.standStaminaDrain(60, 3, 0.5), 1e-6);
        assertEquals(0f, BallBreakerAging.standStaminaDrain(0, 3, 1.0), 1e-6);
    }

    @Test
    void agedBodyOnlyLoses() {
        assertEquals(-4.0, BallBreakerAging.attributePenalty(2.0, 2), 1e-9);
        assertEquals(-4.0, BallBreakerAging.attributePenalty(-2.0, 2), 1e-9, "sign of config value does not matter");
        assertEquals(0.0, BallBreakerAging.attributePenalty(2.0, 0), 1e-9);
    }
}
