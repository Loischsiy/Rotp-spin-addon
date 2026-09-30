package com.loischsiy.rotpspin.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class SteelBallDeflectTest {
    private static final UUID OWNER = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();

    @Test
    void otherStandDropsActiveBall() {
        assertTrue(SteelBallEntity.shouldDropOnStandParry(true, OWNER, OTHER));
    }

    @Test
    void ownStandSparesBall() {
        assertFalse(SteelBallEntity.shouldDropOnStandParry(true, OWNER, OWNER));
    }

    @Test
    void plainBallIgnored() {
        assertFalse(SteelBallEntity.shouldDropOnStandParry(false, OWNER, OTHER));
    }

    @Test
    void unknownOwnerDrops() {
        assertTrue(SteelBallEntity.shouldDropOnStandParry(true, null, OTHER));
    }

    @Test
    void unknownStandUserDrops() {
        assertTrue(SteelBallEntity.shouldDropOnStandParry(true, OWNER, null));
    }
}
