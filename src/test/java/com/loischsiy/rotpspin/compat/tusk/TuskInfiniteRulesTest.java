package com.loischsiy.rotpspin.compat.tusk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TuskInfiniteRulesTest {
    @Test
    void chargeNeedsAct4Lesson5AndSuperSpin() {
        assertTrue(TuskInfiniteRules.grantsCharge(true, false, 3, 5, 5, true));
        assertFalse(TuskInfiniteRules.grantsCharge(true, false, 2, 5, 5, true));
        assertFalse(TuskInfiniteRules.grantsCharge(true, false, 3, 4, 5, true));
        assertFalse(TuskInfiniteRules.grantsCharge(true, false, 3, 5, 5, false));
    }

    @Test
    void noChargeWhenDisabledOrAlreadyCharged() {
        assertFalse(TuskInfiniteRules.grantsCharge(false, false, 3, 5, 5, true));
        assertFalse(TuskInfiniteRules.grantsCharge(true, true, 3, 5, 5, true));
    }

    @Test
    void counterRotationNeedsSuperSpinBall() {
        assertTrue(TuskInfiniteRules.counterRotates(true, true, false, 5, 5, true, true));
        assertFalse(TuskInfiniteRules.counterRotates(true, true, false, 5, 5, true, false));
        assertFalse(TuskInfiniteRules.counterRotates(true, true, true, 5, 5, true, true));
        assertFalse(TuskInfiniteRules.counterRotates(true, false, false, 5, 5, true, true));
        assertFalse(TuskInfiniteRules.counterRotates(true, true, false, 4, 5, true, true));
    }

    @Test
    void counterRotationWithoutSuperSpinWhenConfigured() {
        assertTrue(TuskInfiniteRules.counterRotates(true, true, false, 5, 5, false, false));
        assertFalse(TuskInfiniteRules.counterRotates(false, true, false, 5, 5, false, true));
    }
}
