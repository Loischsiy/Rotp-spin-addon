package com.loischsiy.rotpspin.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CloakAnimTest {
    private static final float EPS = 1e-4F;

    @Test
    void standingStillHangsNearlyStraight() {
        assertEquals(6.0F, CloakAnim.drag(0, 0, 0, 0), EPS);
    }

    @Test
    void runningAndFallingLiftTheClothCapped() {
        float walk = CloakAnim.drag(0.2, 0, 0, 0);
        assertTrue(walk > CloakAnim.drag(0, 0, 0, 0));
        assertEquals(CloakAnim.MAX_LIFT, CloakAnim.drag(5, 5, 0, 0), EPS);
        assertTrue(CloakAnim.drag(0, 1.0, 0, 0) > 6.0F, "falling: the cloth lags above");
        assertEquals(6.0F, CloakAnim.drag(-1, 0, 0, 0), EPS, "walking backwards never pulls it into the body");
    }

    @Test
    void segmentsFlutterOutOfPhase() {
        float a = CloakAnim.pitch(0, 6, 0, 10);
        float b = CloakAnim.pitch(1, 6, 0, 10);
        float c = CloakAnim.pitch(2, 6, 0, 10);
        assertNotEquals(a, b, EPS);
        assertNotEquals(b, c, EPS);
        for (float age = 0; age < 200; age += 0.5F) {
            float p = CloakAnim.pitch(1, 6, 0, age);
            assertTrue(p >= 6 - CloakAnim.IDLE_FLUTTER - CloakAnim.MOVE_FLUTTER && p <= 6 + 9, "idle stays small");
        }
    }

    @Test
    void openSailLiftsAndFans() {
        for (int i = 0; i < CloakAnim.SEGMENTS; i++) {
            assertTrue(CloakAnim.pitch(i, 6, 1, 3) >= CloakAnim.SAIL_LIFT - CloakAnim.SAIL_FLUTTER - EPS);
            assertTrue(CloakAnim.pitch(i, 6, 1, 3) <= CloakAnim.MAX_PITCH);
        }
        assertEquals(CloakAnim.SAIL_SPREAD, CloakAnim.roll(0, 0, 1), EPS);
        assertEquals(0.0F, CloakAnim.roll(1, 0, 1), EPS);
        assertEquals(-CloakAnim.SAIL_SPREAD, CloakAnim.roll(2, 0, 1), EPS);
        assertEquals(0.0F, CloakAnim.roll(0, 0, 0), EPS, "folded: no fan");
    }

    @Test
    void leanIsHalfOfClampedSideLag() {
        assertEquals(10.0F, CloakAnim.lean(1.0), EPS);
        assertEquals(-5.0F, CloakAnim.lean(-0.1), EPS);
        assertEquals(3.0F, CloakAnim.roll(1, 3.0F, 0), EPS);
    }

    @Test
    void approachOpensInFiveTicks() {
        float open = 0;
        for (int tick = 0; tick < 5; tick++) {
            open = CloakAnim.approach(open, 1, CloakAnim.OPEN_PER_TICK);
        }
        assertEquals(1.0F, open, EPS);
        assertEquals(0.8F, CloakAnim.approach(1, 0, 0.2F), EPS);
        assertEquals(0.0F, CloakAnim.approach(0.1F, 0, 0.2F), EPS);
    }
}
