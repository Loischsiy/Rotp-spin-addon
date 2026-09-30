package com.loischsiy.rotpspin.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SpinGoldenTest {
    private static final double EPS = 1e-9;
    private static final Set<String> DEAD =
            new HashSet<>(Arrays.asList("NETHER", "THEEND", "ICY", "DESERT", "NONE"));

    @Test
    void belowLessonFourNeverCalibrated() {
        assertFalse(SpinGolden.isCalibrated(3, "PLAINS", true, DEAD));
        assertFalse(SpinGolden.isCalibrated(1, "PLAINS", false, DEAD));
    }

    @Test
    void lessonFourNeedsLivingBiomeOrBuckle() {
        assertTrue(SpinGolden.isCalibrated(4, "PLAINS", false, DEAD));
        assertFalse(SpinGolden.isCalibrated(4, "ICY", false, DEAD));
        assertFalse(SpinGolden.isCalibrated(4, "DESERT", false, DEAD));
        assertTrue(SpinGolden.isCalibrated(4, "ICY", true, DEAD));
        assertTrue(SpinGolden.isCalibrated(4, "NETHER", true, DEAD));
    }

    @Test
    void snowfallCalibratesDeadBiomeAtLessonFour() {
        // Frozen strait (ch. 54): no markers until the snow starts falling.
        assertFalse(SpinGolden.isCalibrated(4, "ICY", false, false, DEAD));
        assertTrue(SpinGolden.isCalibrated(4, "ICY", false, true, DEAD));
        assertTrue(SpinGolden.isCalibrated(4, "DESERT", false, true, DEAD));
        // Living biome stays calibrated with or without snow.
        assertTrue(SpinGolden.isCalibrated(4, "PLAINS", false, false, DEAD));
    }

    @Test
    void snowfallDoesNotReplaceTheLesson() {
        assertFalse(SpinGolden.isCalibrated(3, "ICY", false, true, DEAD));
        assertFalse(SpinGolden.isCalibrated(1, "PLAINS", true, true, DEAD));
    }

    @Test
    void legacyOverloadMeansNoSnow() {
        assertEquals(SpinGolden.isCalibrated(4, "ICY", false, false, DEAD),
                SpinGolden.isCalibrated(4, "ICY", false, DEAD));
    }

    @Test
    void handFrameLastsForItsDuration() {
        long until = SpinGolden.handFrameUntil(1000L, 600);
        assertEquals(1600L, until);
        assertTrue(SpinGolden.isHandFrameActive(1000L, until));
        assertTrue(SpinGolden.isHandFrameActive(1599L, until));
        assertFalse(SpinGolden.isHandFrameActive(1600L, until));
        // Never framed: game time 0 is not before "until" 0.
        assertFalse(SpinGolden.isHandFrameActive(0L, 0L));
        // Negative duration from a broken config does not extend into the past.
        assertEquals(1000L, SpinGolden.handFrameUntil(1000L, -5));
    }

    @Test
    void lessonFiveCalibratedEverywhere() {
        assertTrue(SpinGolden.isCalibrated(5, "NETHER", false, DEAD));
        assertTrue(SpinGolden.isCalibrated(5, "NONE", false, DEAD));
        assertTrue(SpinGolden.isCalibrated(5, "ICY", false, Collections.emptySet()));
    }

    @Test
    void multipliers() {
        assertEquals(1.0, SpinGolden.multiplier(3, true, 1.5, 2.0), EPS);
        assertEquals(1.0, SpinGolden.multiplier(4, false, 1.5, 2.0), EPS);
        assertEquals(1.5, SpinGolden.multiplier(4, true, 1.5, 2.0), EPS);
        assertEquals(2.0, SpinGolden.multiplier(5, false, 1.5, 2.0), EPS);
        assertEquals(2.0, SpinGolden.multiplier(5, true, 1.5, 2.0), EPS);
    }

    @Test
    void chippedBallKeepsShareOfBonus() {
        assertEquals(1.25, SpinGolden.applyChipped(1.5, 0.5), EPS);
        assertEquals(1.0, SpinGolden.applyChipped(1.0, 0.5), EPS);
        assertEquals(1.5, SpinGolden.applyChipped(1.5, 1.0), EPS);
    }

    @Test
    void gallopGivesSuperSpinFromLessonFour() {
        assertTrue(SpinGolden.isGallopSuperSpin(4, 0.3, 0.25));
        assertTrue(SpinGolden.isGallopSuperSpin(5, 0.3, 0.25));
        assertFalse(SpinGolden.isGallopSuperSpin(4, 0.2, 0.25));
        assertFalse(SpinGolden.isGallopSuperSpin(4, 0.0, 0.25));
        // No skill, no detour: walking the horse is not a gallop.
        assertFalse(SpinGolden.isGallopSuperSpin(3, 1.0, 0.25));
    }
}
