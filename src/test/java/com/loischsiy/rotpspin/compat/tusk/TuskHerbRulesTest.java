package com.loischsiy.rotpspin.compat.tusk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TuskHerbRulesTest {
    @Test
    void growsOnIntervalOnly() {
        assertTrue(TuskHerbRules.growsNailThisTick(100, 100, 3, 10));
        assertTrue(TuskHerbRules.growsNailThisTick(200, 100, 3, 10));
        assertFalse(TuskHerbRules.growsNailThisTick(150, 100, 3, 10));
    }

    @Test
    void noGrowthWhenFullOrExpired() {
        assertFalse(TuskHerbRules.growsNailThisTick(100, 100, 10, 10));
        assertFalse(TuskHerbRules.growsNailThisTick(0, 100, 3, 10));
        assertFalse(TuskHerbRules.growsNailThisTick(100, 0, 3, 10));
    }

    @Test
    void infusionStacksUpToCap() {
        assertEquals(1200, TuskHerbRules.extendInfusion(0, 1200, 3600));
        assertEquals(2400, TuskHerbRules.extendInfusion(1200, 1200, 3600));
        assertEquals(3600, TuskHerbRules.extendInfusion(3000, 1200, 3600));
    }

    @Test
    void infusionNeverShortened() {
        assertEquals(5000, TuskHerbRules.extendInfusion(5000, 1200, 3600));
    }
}
