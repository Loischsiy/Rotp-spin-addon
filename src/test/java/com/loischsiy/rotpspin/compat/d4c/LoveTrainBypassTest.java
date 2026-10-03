package com.loischsiy.rotpspin.compat.d4c;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoveTrainBypassTest {
    @Test
    void liftsOnlyDuringBallBreakerHitOnLoveTrain() {
        assertTrue(LoveTrainBypass.shouldLift(true, true, true));
        assertFalse(LoveTrainBypass.shouldLift(true, false, true));
        assertFalse(LoveTrainBypass.shouldLift(true, true, false));
        assertFalse(LoveTrainBypass.shouldLift(false, true, true));
    }

    @Test
    void pierceWithoutD4cJustRunsTheHit() {
        assertTrue(LoveTrainBypass.pierce(() -> true));
        assertFalse(LoveTrainBypass.pierce(() -> false));
    }

    @Test
    void loveTrainId() {
        assertEquals("rotp_d4c:love_train", LoveTrainBypass.LOVE_TRAIN_ID.toString());
    }

    @Test
    void keepsSenescenceOnlyAgainstLoveTrainCleanse() {
        assertTrue(LoveTrainBypass.shouldKeep(true, true, true, true));
        assertFalse(LoveTrainBypass.shouldKeep(false, true, true, true));
        assertFalse(LoveTrainBypass.shouldKeep(true, false, true, true));
        assertFalse(LoveTrainBypass.shouldKeep(true, true, false, true));
        // milk, commands and other mods still remove it
        assertFalse(LoveTrainBypass.shouldKeep(true, true, true, false));
    }
}
