package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpinBraceTest {
    private static final float EPS = 1e-4F;

    @Test
    void bracedOnlyAfterWindup() {
        assertFalse(SpinBrace.isBraced(4, 5));
        assertTrue(SpinBrace.isBraced(5, 5));
        assertTrue(SpinBrace.isBraced(0, 0));
    }

    @Test
    void onlyKineticBlowsAreBraceable() {
        assertTrue(SpinBrace.isBraceable(true, false, false, false, false, false));  // bullet, arrow
        assertTrue(SpinBrace.isBraceable(false, true, false, false, false, false));  // bomb blast
        assertTrue(SpinBrace.isBraceable(false, false, true, false, false, false));  // melee
        assertFalse(SpinBrace.isBraceable(false, false, false, false, false, false)); // fall, cactus...
        assertFalse(SpinBrace.isBraceable(true, false, false, false, true, false));  // fire arrow
        assertFalse(SpinBrace.isBraceable(false, false, true, true, false, false));  // bypasses armor
        assertFalse(SpinBrace.isBraceable(false, false, true, false, false, true));  // magic
    }

    @Test
    void absorbsConfiguredShare() {
        assertEquals(6, SpinBrace.absorbed(10, 0.6F, 100, 4), EPS);
    }

    @Test
    void neverInvulnerable() {
        assertEquals(10 * SpinBrace.MAX_REDUCTION, SpinBrace.absorbed(10, 1.0F, 1000, 0), EPS);
    }

    @Test
    void limitedByEnergy() {
        assertEquals(2.5F, SpinBrace.absorbed(10, 0.6F, 10, 4), EPS);
        assertEquals(0, SpinBrace.absorbed(10, 0.6F, 0, 4), EPS);
    }

    @Test
    void freeWhenNoEnergyPrice() {
        assertEquals(3, SpinBrace.absorbed(5, 0.6F, 0, 0), EPS);
    }

    @Test
    void nothingToAbsorb() {
        assertEquals(0, SpinBrace.absorbed(0, 0.6F, 100, 4), EPS);
        assertEquals(0, SpinBrace.absorbed(10, 0, 100, 4), EPS);
    }

    @Test
    void energyCostNeverExceedsEnergy() {
        assertEquals(8F, SpinBrace.energyCost(2F, 4F, 100F), EPS);
        float energy = 1F;
        float absorbed = SpinBrace.absorbed(10F, 0.7F, energy, 3F);
        assertTrue(SpinBrace.energyCost(absorbed, 3F, energy) <= energy);
        assertEquals(0F, SpinBrace.energyCost(2F, 0F, 100F), EPS);
        assertEquals(0F, SpinBrace.energyCost(0F, 4F, 100F), EPS);
    }
}
