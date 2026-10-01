package com.loischsiy.rotpspin.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class GyroSpawnRulesTest {
    private static final List<String> PRAIRIE = Arrays.asList("PLAINS", " savanna ");

    @Test
    void categoryMatchIgnoresCaseAndSpaces() {
        assertTrue(GyroSpawnRules.categoryListed("PLAINS", PRAIRIE));
        assertTrue(GyroSpawnRules.categoryListed("SAVANNA", PRAIRIE));
        assertTrue(GyroSpawnRules.categoryListed("plains", PRAIRIE));
    }

    @Test
    void unlistedOrMissingCategoryIsRejected() {
        assertFalse(GyroSpawnRules.categoryListed("JUNGLE", PRAIRIE));
        assertFalse(GyroSpawnRules.categoryListed("PLAINS", Collections.<String>emptyList()));
        assertFalse(GyroSpawnRules.categoryListed(null, PRAIRIE));
        assertFalse(GyroSpawnRules.categoryListed("PLAINS", null));
    }

    @Test
    void checkTickFollowsInterval() {
        assertTrue(GyroSpawnRules.isCheckTick(2400, 1200));
        assertFalse(GyroSpawnRules.isCheckTick(2401, 1200));
        assertFalse(GyroSpawnRules.isCheckTick(0, 0));
    }

    @Test
    void rollRespectsChanceBounds() {
        assertFalse(GyroSpawnRules.rolled(0.0, 0.0));
        assertTrue(GyroSpawnRules.rolled(0.999, 1.0));
        assertTrue(GyroSpawnRules.rolled(0.2, 0.25));
        assertFalse(GyroSpawnRules.rolled(0.3, 0.25));
    }

    @Test
    void spreadStaysInsideBounds() {
        assertEquals(-8, GyroSpawnRules.spreadOffset(0.0, 8));
        assertEquals(8, GyroSpawnRules.spreadOffset(0.99999, 8));
        assertEquals(0, GyroSpawnRules.spreadOffset(0.5, 8));
        assertEquals(0, GyroSpawnRules.spreadOffset(0.7, 0));
    }

    @Test
    void horseStaysInGyrosChunk() {
        assertEquals(1.5, GyroSpawnRules.sameChunkOffset(4.5, 1.5), 1e-9);
        assertEquals(-1.5, GyroSpawnRules.sameChunkOffset(15.5, 1.5), 1e-9);
        assertEquals(1.5, GyroSpawnRules.sameChunkOffset(-16.0, 1.5), 1e-9);
        assertEquals(-1.5, GyroSpawnRules.sameChunkOffset(-0.5, 1.5), 1e-9);
        assertEquals(8.0, GyroSpawnRules.sameChunkOffset(663.5, 8.0), 1e-9);
        assertEquals(-8.0, GyroSpawnRules.sameChunkOffset(670.5, 8.0), 1e-9);
        assertEquals(0.0, GyroSpawnRules.sameChunkOffset(7.5, 0.0), 1e-9);
    }
}
